package org.hawkular.bus.sample.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.MessageListener;

import org.hawkular.bus.common.SimpleBasicMessage;
import org.hawkular.bus.common.consumer.BasicMessageListener;
import org.jboss.logging.Logger;

@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "ExampleQueueName"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "MyFilter = 'fnf'") })
public class MyMDB extends BasicMessageListener<SimpleBasicMessage> {
    private final Logger log = Logger.getLogger(MyMDB.class);

    protected void onBasicMessage(SimpleBasicMessage msg) {
        log.infof("===> MDB received message [%s]", msg);
    };
}
