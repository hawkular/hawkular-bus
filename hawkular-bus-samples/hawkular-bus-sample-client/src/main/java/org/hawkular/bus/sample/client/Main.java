package org.hawkular.bus.sample.client;

import org.hawkular.bus.common.ConnectionContextFactory;
import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.MessageProcessor;
import org.hawkular.bus.common.SimpleBasicMessage;
import org.hawkular.bus.common.consumer.BasicMessageListener;
import org.hawkular.bus.common.consumer.ConsumerConnectionContext;
import org.hawkular.bus.common.producer.ProducerConnectionContext;

/**
 * A simple sample client used to show the API needed to consume and produce messages.
 *
 * @author Heiko W. Rupp
 */
public class Main {
    private static final String BROKER_URL = "vm://mybroker?broker.persistent=false";
    private static final Endpoint ENDPOINT = new Endpoint(Endpoint.Type.QUEUE, "myqueue");

    public static void main(String[] args) throws Exception {
        Consumer consumer = new Consumer();
        Producer producer = new Producer();

        consumer.consume();
        producer.produce();

        Thread.sleep(1000); // give some time for message to flow before shutting down

        consumer.cleanUp();
        producer.cleanUp();
    }

    private static class Consumer {
        ConnectionContextFactory cachedFactory;

        public void consume() throws Exception {
            ConnectionContextFactory factory = new ConnectionContextFactory(BROKER_URL);
            ConsumerConnectionContext context = factory.createConsumerConnectionContext(ENDPOINT);
            BasicMessageListener<SimpleBasicMessage> listener = new BasicMessageListener<SimpleBasicMessage>() {
                @Override
                protected void onBasicMessage(SimpleBasicMessage msg) {
                    System.out.println("Consumed message===>" + msg.getMessage());
                }
            };
            MessageProcessor processor = new MessageProcessor();
            processor.listen(context, listener);

            // remember this so we can clean up after ourselves later
            this.cachedFactory = factory;
        }

        public void cleanUp() throws Exception {
            this.cachedFactory.close();
        }
    }

    private static class Producer {
        ConnectionContextFactory cachedFactory;

        public void produce() throws Exception {
            ConnectionContextFactory factory = new ConnectionContextFactory(BROKER_URL);
            ProducerConnectionContext pc = factory.createProducerConnectionContext(ENDPOINT);
            SimpleBasicMessage msg = new SimpleBasicMessage("hello from " + Main.class);
            MessageProcessor processor = new MessageProcessor();
            processor.send(pc, msg);

            // remember this so we can clean up after ourselves later
            this.cachedFactory = factory;
        }

        public void cleanUp() throws Exception {
            this.cachedFactory.close();
        }
    }
}
