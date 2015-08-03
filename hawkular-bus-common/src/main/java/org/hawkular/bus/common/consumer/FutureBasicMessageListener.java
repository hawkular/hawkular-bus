/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.bus.common.consumer;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.bus.common.BasicMessageWithExtraData;
import org.hawkular.bus.common.log.MsgLogger;
import org.jboss.logging.Logger;

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * This listener waits for a single incoming message and returns it within the context of the Future API.
 *
 * Once the message is received, the consumer associated with this listener will be closed.
 *
 * This is not intended to receive a series of messages; it is only expected that the consumer will receive a single
 * message. This is useful, for example, to process a response from a single RPC call over a temporary queue.
 *
 * To use this, just register this as a message listener and call one of the get() methods to block waiting for the
 * response.
 *
 * @author John Mazzitelli
 *
 * @param <T> the type of message that is expected to be received
 */
public class FutureBasicMessageListener<T extends BasicMessage> extends BasicMessageListener<T> implements
        ListenableFuture<BasicMessageWithExtraData<T>> {

    private final MsgLogger msglog = MsgLogger.LOGGER;

    private static enum State {
        WAITING, DONE, CANCELLED
    }

    // use array blocking queue because it has the same concurrent semantics as Future making things easy for us
    private final BlockingQueue<BasicMessageWithExtraData<T>> responseQ = new ArrayBlockingQueue<>(1);
    private BasicMessageWithExtraData<T> responseMessage = null;
    private State state = State.WAITING;

    // The execution list to hold our executors.
    private final ExecutionList executionList = new ExecutionList();

    public FutureBasicMessageListener() {
        super();
    }

    public FutureBasicMessageListener(Class<T> jsonDecoderRing) {
        super(jsonDecoderRing);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // by rule, if we are already done, this method should return false
        if (isDone()) {
            return false;
        }

        try {
            if (mayInterruptIfRunning) {
                closeConsumer();
                state = State.CANCELLED;
            } else {
                msglog.errorCannotCancelRunningFuture();
            }
        } catch (Exception e) {
            msglog.errorConsumerCloseFailureOnFutureCancel();
        }

        executionList.execute();

        return state == State.CANCELLED;
    }

    @Override
    public boolean isCancelled() {
        return state == State.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state == State.DONE || state == State.CANCELLED;
    }

    @Override
    public BasicMessageWithExtraData<T> get() throws InterruptedException, ExecutionException {
        if (state == State.CANCELLED) {
            throw new CancellationException();
        }

        // Since we only ever expect a single response, only take the first item ever in the blocking Q.
        // From there on out we never take from the blocking Q.
        if (responseMessage == null) {
            responseMessage = responseQ.take();
            state = State.DONE; // since we have a response, set state to DONE to ensure our caller knows our state
        }
        return responseMessage;
    }

    @Override
    public BasicMessageWithExtraData<T> get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (state == State.CANCELLED) {
            throw new CancellationException();
        }

        // Since we only ever expect a single response, only take the first item ever in the blocking Q.
        // From there on out we never take from the blocking Q.
        if (responseMessage == null) {
            final BasicMessageWithExtraData<T> item = responseQ.poll(timeout, unit);
            if (item == null) {
                throw new TimeoutException();
            }
            responseMessage = item;
            state = State.DONE; // since we have a response, set state to DONE to ensure our caller knows our state
        }

        return responseMessage;
    }

    @Override
    public void addListener(Runnable listener, Executor exec) {
        executionList.add(listener, exec);
    }

    @Override
    protected void onBasicMessage(BasicMessageWithExtraData<T> msgWithData) {
        // if we already got a message or were cancelled, ignore any additional messages we might receive
        if (!isDone()) {
            state = State.DONE;
            if (responseQ.offer(msgWithData)) {
                executionList.execute();
            } else {
                msglog.errorCannotStoreIncomingMessageFutureInvalid();
                state = State.CANCELLED;
            }
            try {
                closeConsumer();
            } catch (Exception e) {
                msglog.errorFailedToCloseFutureConsumer(e);
            }
        }
    }

    protected void closeConsumer() throws IOException {
        ConsumerConnectionContext cc = getConsumerConnectionContext();
        if (cc != null) {
            Logger.getLogger(getClass()).debugf("Future listener closing consumer on destination [%s]",
                    cc.getDestination());
            cc.close();
        }
        return;
    }

    @Override
    public String toString() {
        return super.toString() + ": STATE=" + this.state;
    }
}
