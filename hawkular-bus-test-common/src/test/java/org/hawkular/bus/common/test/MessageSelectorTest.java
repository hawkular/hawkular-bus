package org.hawkular.bus.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.hawkular.bus.common.ConnectionContextFactory;
import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.Endpoint.Type;
import org.hawkular.bus.common.MessageProcessor;
import org.hawkular.bus.common.consumer.ConsumerConnectionContext;
import org.hawkular.bus.common.producer.ProducerConnectionContext;
import org.junit.Test;

/**
 * Tests message selectors and filtering of messagings.
 */
public class MessageSelectorTest {
    @Test
    public void testFilter() throws Exception {
        ConnectionContextFactory consumerFactory = null;
        ConnectionContextFactory producerFactory = null;

        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
        broker.start();

        try {
            String brokerURL = broker.getBrokerURL();
            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");
            HashMap<String, String> myTestHeaderBoo = new HashMap<String, String>();
            HashMap<String, String> myTestHeaderOther = new HashMap<String, String>();
            myTestHeaderBoo.put("MyTest", "boo");
            myTestHeaderOther.put("MyTest", "Other");

            // mimic server-side
            consumerFactory = new ConnectionContextFactory(brokerURL);
            ConsumerConnectionContext consumerContext = consumerFactory.createConsumerConnectionContext(endpoint, "MyTest = 'boo'");
            SimpleTestListener<SpecificMessage> listener = new SimpleTestListener<SpecificMessage>(SpecificMessage.class);
            MessageProcessor serverSideProcessor = new MessageProcessor();
            serverSideProcessor.listen(consumerContext, listener);

            // mimic client side
            producerFactory = new ConnectionContextFactory(brokerURL);
            ProducerConnectionContext producerContext = producerFactory.createProducerConnectionContext(endpoint);
            MessageProcessor clientSideProcessor = new MessageProcessor();

            // send one that won't match the selector
            SpecificMessage specificMessage = new SpecificMessage("nope", null, "no match");
            clientSideProcessor.send(producerContext, specificMessage, myTestHeaderOther);

            // wait for the message to flow - we won't get it because our selector doesn't match
            listener.waitForMessage(3); // 3 seconds is plenty of time to realize we aren't getting it
            assertTrue("Should not have received the message", listener.getReceivedMessage() == null);

            // send one that will match the selector
            specificMessage = new SpecificMessage("hello", null, "specific text");
            clientSideProcessor.send(producerContext, specificMessage, myTestHeaderBoo);

            // wait for the message to flow - we should get it now
            listener.waitForMessage(3);
            assertEquals("Should have received the message", listener.getReceivedMessage().getSpecific(), "specific text");

        } finally {
            // close everything
            producerFactory.close();
            consumerFactory.close();
            broker.stop();
        }
    }
}
