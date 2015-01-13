package org.hawkular.bus.common.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.bus.common.consumer.BasicMessageListener;

/**
 * Simple test listener that allows you to wait for a message and when it comes in you can retrieve it. See
 * {@link #waitForMessage(long)} and {@link #getBasicMessage()}. This can retrieve multiple messages serially, but if you
 * don't retrieve a message before a new one comes in, the first message is lost.
 *
 * This class is not thread safe. Its purpose is just to fascilitate unit tests.
 *
 * @param <T>
 *            the expected message type
 */
public class SimpleTestListener<T extends BasicMessage> extends BasicMessageListener<T> {
    private CountDownLatch latch = new CountDownLatch(1);
    public T message;

    public SimpleTestListener(Class<T> clazz) {
        super(clazz);
    }

    public boolean waitForMessage(long secs) throws InterruptedException {
        return latch.await(secs, TimeUnit.SECONDS);
    }

    public T getReceivedMessage() {
        T result = null;
        if (message != null) {
            result = message;
            // reset the listener to get ready for the next message
            latch = new CountDownLatch(1);
            message = null;
        }
        return result;
    }

    @Override
    protected void onBasicMessage(T message) {
        this.message = message;
        latch.countDown();
    }
}
