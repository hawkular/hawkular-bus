package org.hawkular.bus.mdb;

import javax.jms.ConnectionFactory;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.bus.common.ConnectionContextFactory;
import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.consumer.ConsumerConnectionContext;
import org.hawkular.bus.common.consumer.RPCBasicMessageListener;
import org.jboss.logging.Logger;

public abstract class RPCBasicMessageDrivenBean<T extends BasicMessage, U extends BasicMessage> extends
        RPCBasicMessageListener<T, U> {
    private final Logger log = Logger.getLogger(RPCBasicMessageDrivenBean.class);

    /**
     * MDB subclasses need to define this usually by returning a factory that is obtained through injection:
     *
     * <pre>
     * &#064;Resource(mappedName = &quot;java:/HawkularBusConnectionFactory&quot;)
     * private ConnectionFactory connectionFactory;
     * </pre>
     *
     * @return connection factory to be used when sending the response
     */
    public abstract ConnectionFactory getConnectionFactory();

    @Override
    public ConsumerConnectionContext getConsumerConnectionContext() {
        ConsumerConnectionContext ctx = null;
        try {
            // here we build a faux consumer connection context whose data will be duplicated in
            // a producer connection context so the response message can be sent. The endpoint we
            // use here is just a dummy one - it will be replaced with the JMS ReplyTo by the superclass.
            ConnectionContextFactory ccf = new ConnectionContextFactory(getConnectionFactory());
            ctx = ccf.createConsumerConnectionContext(Endpoint.TEMPORARY_QUEUE);
        } catch (Exception e) {
            log.error("Failed to build context - will not be able to respond to message", e);
        }
        return ctx;
    }
}