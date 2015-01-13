package org.hawkular.bus.common.consumer;

import javax.jms.Message;

import org.hawkular.bus.common.BasicMessage;

/**
 * A message listener that expects to receive a JSON-encoded BasicMessage or one of its subclasses. Implementors need
 * only implement the method that takes an BasicRecord or one of its subclasses; the JSON decoding is handled for you.
 * 
 * This processes fire-and-forget requests - that is, the request message is processed with no response being sent back
 * to the sender.
 * 
 * @author John Mazzitelli
 */

public abstract class BasicMessageListener<T extends BasicMessage> extends AbstractBasicMessageListener<T> {

    public BasicMessageListener() {
        super();
    }

    protected BasicMessageListener(Class<T> jsonDecoderRing) {
        super(jsonDecoderRing);
    }

    @Override
    public void onMessage(Message message) {
        T basicMessage = getBasicMessageFromMessage(message);
        if (basicMessage == null) {
            return; // either we are not to process this message or some error occurred, so we skip it
        }

        onBasicMessage(basicMessage);
        return;
    };

    /**
     * Subclasses implement this method to process the received message.
     * 
     * @param message
     *            the message to process
     */
    protected abstract void onBasicMessage(T basicMessage);
}
