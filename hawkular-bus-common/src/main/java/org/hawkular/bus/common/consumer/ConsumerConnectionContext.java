package org.hawkular.bus.common.consumer;

import javax.jms.MessageConsumer;

import org.hawkular.bus.common.ConnectionContext;

public class ConsumerConnectionContext extends ConnectionContext {
    private MessageConsumer consumer;

    public MessageConsumer getMessageConsumer() {
        return consumer;
    }

    public void setMessageConsumer(MessageConsumer consumer) {
        this.consumer = consumer;
    }
}
