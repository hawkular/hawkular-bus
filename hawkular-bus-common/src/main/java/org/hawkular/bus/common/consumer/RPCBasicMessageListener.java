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

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.bus.common.MessageProcessor;
import org.hawkular.bus.common.producer.ProducerConnectionContext;

/**
 * A listener that processes an incoming request that will require a response sent back to the sender of the request.
 *
 * @author John Mazzitelli
 *
 * @param <T>
 *            the type of the incoming request message
 * @param <U>
 *            the type of the response message that is to be sent back to the request sender
 */
public abstract class RPCBasicMessageListener<T extends BasicMessage, U extends BasicMessage> extends
        AbstractBasicMessageListener<T> {

    // this will be used to send our reply
    private MessageProcessor messageSender;

    /**
     * Initialize with a default message sender.
     */
    public RPCBasicMessageListener() {
        super();
        setMessageSender(new MessageProcessor());
    }

    public RPCBasicMessageListener(MessageProcessor messageSender) {
        super();
        setMessageSender(messageSender);
    }

    protected RPCBasicMessageListener(Class<T> jsonDecoderRing) {
        super(jsonDecoderRing);
        setMessageSender(new MessageProcessor());
    }

    protected RPCBasicMessageListener(Class<T> jsonDecoderRing, MessageProcessor messageSender) {
        super(jsonDecoderRing);
        setMessageSender(messageSender);
    }

    protected MessageProcessor getMessageSender() {
        return messageSender;
    }

    protected void setMessageSender(MessageProcessor messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public void onMessage(Message message) {
        T basicMessage = getBasicMessageFromMessage(message);
        if (basicMessage == null) {
            return; // either we are not to process this message or some error occurred, so we skip it
        }

        U responseBasicMessage = onBasicMessage(basicMessage);

        // send the response back to the sender of the request
        try {
            Destination replyTo = message.getJMSReplyTo();

            if (replyTo != null) {
                MessageProcessor sender = getMessageSender();
                if (sender == null) {
                    getLog().error("Cannot return response - there is no message sender assigned to this listener");
                    return;
                }

                ConsumerConnectionContext consumerConnectionContext = getConsumerConnectionContext();
                if (consumerConnectionContext == null) {
                    getLog().error("Cannot return response - there is no connection context assigned to this listener");
                    return;
                }

                // create a producer connection context so it uses the same connection information as our consumer, but
                // ensure that we send the response to where the client told us to send it.
                ProducerConnectionContext producerContext = new ProducerConnectionContext();
                producerContext.copy(consumerConnectionContext);
                producerContext.setDestination(replyTo);
                Session session = producerContext.getSession();
                if (session == null) {
                    getLog().error(
                            "Cannot return response - there is no session in the connection context assigned to this"
                            + " listener");
                    return;
                }
                producerContext.setMessageProducer(session.createProducer(replyTo));

                sender.send(producerContext, responseBasicMessage);

            } else {
                getLog().debug("Sender did not tell us where to reply - will not send any response back");
            }
        } catch (Exception e) {
            getLog().error("Failed to send response", e);
            return;
        }
    }

    /**
     * Subclasses implement this method to process the received message.
     *
     * @param message
     *            the message to process
     * @return the response message - this will be forwarded to the sender of the request message
     */
    protected abstract U onBasicMessage(T basicMessage);

}
