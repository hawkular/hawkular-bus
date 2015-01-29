package org.hawkular.bus.sample.mdb;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;

import org.hawkular.bus.common.SimpleBasicMessage;
import org.hawkular.bus.mdb.RPCBasicMessageDrivenBean;
import org.jboss.logging.Logger;

@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "ExampleQueueName"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "MyFilter = 'rpc'") })
public class MyRPCMDB extends RPCBasicMessageDrivenBean<SimpleBasicMessage, SimpleBasicMessage> {
    private final Logger log = Logger.getLogger(MyRPCMDB.class);

    @Resource(mappedName = "java:/HawkularBusConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Override
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    protected SimpleBasicMessage onBasicMessage(SimpleBasicMessage msg) {
        log.infof("===> MDB received incoming RPC message [%s]", msg);
        SimpleBasicMessage response = new SimpleBasicMessage("ECHO! " + msg.getMessage());
        log.infof("===> MDB sending response RPC message [%s]", response);
        return response;
    };
}
