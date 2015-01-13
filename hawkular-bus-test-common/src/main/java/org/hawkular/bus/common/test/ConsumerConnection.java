package org.hawkular.bus.common.test;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.hawkular.bus.common.ConnectionContextFactory;
import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.consumer.ConsumerConnectionContext;

/**
 * Convenience class tests can use to create a consumer of either topic or queue
 * messages from a broker.
 * 
 * The constructor creates the connection and attaches the listener after which
 * the listener can start consuming messages as they are produced.
 */
public class ConsumerConnection extends ConnectionContextFactory {

    private ConsumerConnectionContext ccc;

    public ConsumerConnection(String brokerURL, Endpoint endpoint, MessageListener messageListener) throws JMSException {
        super(brokerURL);
        prepareConsumer(brokerURL, endpoint, messageListener);
    }

    protected void prepareConsumer(String brokerURL, Endpoint endpoint, MessageListener messageListener) throws JMSException {
        ccc = new ConsumerConnectionContext();
        createConnection(ccc);
        setConnection(ccc.getConnection());
        getConnection().start();
        createSession(ccc);
        createDestination(ccc, endpoint);
        MessageConsumer consumer = ccc.getSession().createConsumer(ccc.getDestination());
        consumer.setMessageListener(messageListener);
    }

    public ConsumerConnectionContext getConsumerConnectionContext() {
        return ccc;
    }
}
