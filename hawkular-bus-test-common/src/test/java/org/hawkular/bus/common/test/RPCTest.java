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
package org.hawkular.bus.common.test;

import org.hawkular.bus.common.BasicMessageWithExtraData;
import org.hawkular.bus.common.consumer.RPCBasicMessageListener;

/**
 * Tests request-response messaging.
 */
public class RPCTest {
//    @Test
//    public void testSendRPC() throws Exception {
//        ConnectionContextFactory consumerFactory = null;
//        ConnectionContextFactory producerFactory = null;
//
//        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
//        broker.start();
//
//        try {
//            String brokerURL = broker.getBrokerURL();
//            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");
//
//            Map<String, String> details = new HashMap<String, String>();
//            details.put("key1", "val1");
//            details.put("secondkey", "secondval");
//            SpecificMessage specificMessage = new SpecificMessage("hello", details, "specific text");
//
//            // mimic server-side - this will receive the initial request message (and will send the response back)
//            consumerFactory = new ConnectionContextFactory(brokerURL);
//            ConsumerConnectionContext consumerContext = consumerFactory.createConsumerConnectionContext(endpoint);
//            TestRPCListener requestListener = new TestRPCListener();
//            MessageProcessor serverSideProcessor = new MessageProcessor();
//            serverSideProcessor.listen(consumerContext, requestListener);
//
//            // mimic client side - this will send the initial request message and receive the response from the server
//            producerFactory = new ConnectionContextFactory(brokerURL);
//            ProducerConnectionContext producerContext = producerFactory.createProducerConnectionContext(endpoint);
//            MessageProcessor clientSideProcessor = new MessageProcessor();
//            ListenableFuture<BasicMessageWithExtraData<SpecificMessage>> future = clientSideProcessor.sendRPC(
//                    producerContext, specificMessage,
//                    SpecificMessage.class);
//
//            // wait for the message to flow
//            BasicMessageWithExtraData<SpecificMessage> receivedSpecificMessage = null;
//            try {
//                receivedSpecificMessage = future.get();
//            } catch (Exception e) {
//                assert false : "Future failed to obtain response message: " + e;
//            }
//
//            // make sure the message flowed properly
//            assertNotNull("Didn't receive response", receivedSpecificMessage);
//            assertNotNull("Didn't receive response", receivedSpecificMessage.getBasicMessage());
//            assertEquals("RESPONSE:" + specificMessage.getMessage(), receivedSpecificMessage.getBasicMessage()
//                    .getMessage());
//            assertEquals(specificMessage.getDetails(), receivedSpecificMessage.getBasicMessage().getDetails());
//            assertEquals("RESPONSE:" + specificMessage.getSpecific(), receivedSpecificMessage.getBasicMessage()
//                    .getSpecific());
//            assertFalse(future.isCancelled());
//            assertTrue("Future should have been done: " + future, future.isDone());
//
//            // use the future.get(timeout) method and make sure it returns the same
//            try {
//                receivedSpecificMessage = future.get(30, TimeUnit.SECONDS);
//            } catch (Exception e) {
//                assert false : "Future failed to obtain response message: " + e;
//            }
//
//            assertNotNull("Didn't receive response", receivedSpecificMessage);
//            assertNotNull("Didn't receive response", receivedSpecificMessage.getBasicMessage());
//            assertEquals("RESPONSE:" + specificMessage.getMessage(), receivedSpecificMessage.getBasicMessage()
//                    .getMessage());
//            assertEquals(specificMessage.getDetails(), receivedSpecificMessage.getBasicMessage().getDetails());
//            assertEquals("RESPONSE:" + specificMessage.getSpecific(), receivedSpecificMessage.getBasicMessage()
//                    .getSpecific());
//
//        } finally {
//            // close everything
//            producerFactory.close();
//            consumerFactory.close();
//            broker.stop();
//        }
//    }

//    @Test
//    public void testSendRPCAndUseListenableFuture() throws Exception {
//        ConnectionContextFactory consumerFactory = null;
//        ConnectionContextFactory producerFactory = null;
//
//        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
//        broker.start();
//
//        try {
//            String brokerURL = broker.getBrokerURL();
//            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");
//
//            Map<String, String> details = new HashMap<String, String>();
//            details.put("key1", "val1");
//            details.put("secondkey", "secondval");
//            SpecificMessage specificMessage = new SpecificMessage("hello", details, "specific text");
//
//            // mimic server-side - this will receive the initial request message (and will send the response back)
//            consumerFactory = new ConnectionContextFactory(brokerURL);
//            ConsumerConnectionContext consumerContext = consumerFactory.createConsumerConnectionContext(endpoint);
//            TestRPCListener requestListener = new TestRPCListener();
//            MessageProcessor serverSideProcessor = new MessageProcessor();
//            serverSideProcessor.listen(consumerContext, requestListener);
//
//            // mimic client side - this will send the initial request message and receive the response from the server
//            producerFactory = new ConnectionContextFactory(brokerURL);
//            ProducerConnectionContext producerContext = producerFactory.createProducerConnectionContext(endpoint);
//            MessageProcessor clientSideProcessor = new MessageProcessor();
//            ListenableFuture<BasicMessageWithExtraData<SpecificMessage>> future = clientSideProcessor.sendRPC(
//                    producerContext, specificMessage, SpecificMessage.class);
//            TestFutureCallback futureCallback = new TestFutureCallback();
//            Futures.addCallback(future, futureCallback);
//
//            // wait for the message to flow
//            BasicMessageWithExtraData<SpecificMessage> receivedSpecificMessage = null;
//            try {
//                receivedSpecificMessage = futureCallback.getResult(30, TimeUnit.SECONDS);
//            } catch (Throwable t) {
//                assert false : "Future failed to obtain response message: " + t;
//            }
//
//            // make sure the message flowed properly
//            assertFalse(future.isCancelled());
//            assertTrue("Future should have been done: " + future, future.isDone());
//            assertNotNull("Didn't receive response", receivedSpecificMessage);
//            assertEquals("RESPONSE:" + specificMessage.getMessage(), receivedSpecificMessage.getBasicMessage()
//                    .getMessage());
//            assertEquals(specificMessage.getDetails(), receivedSpecificMessage.getBasicMessage().getDetails());
//            assertEquals("RESPONSE:" + specificMessage.getSpecific(), receivedSpecificMessage.getBasicMessage()
//                    .getSpecific());
//
//            // use the future.get(timeout) method and make sure it returns the same
//            try {
//                receivedSpecificMessage = future.get(30, TimeUnit.SECONDS);
//            } catch (Exception e) {
//                assert false : "Future failed to obtain response message: " + e;
//            }
//
//            assertNotNull("Didn't receive response", receivedSpecificMessage);
//            assertEquals("RESPONSE:" + specificMessage.getMessage(), receivedSpecificMessage.getBasicMessage()
//                    .getMessage());
//            assertEquals(specificMessage.getDetails(), receivedSpecificMessage.getBasicMessage().getDetails());
//            assertEquals("RESPONSE:" + specificMessage.getSpecific(), receivedSpecificMessage.getBasicMessage()
//                    .getSpecific());
//
//        } finally {
//            // close everything
//            producerFactory.close();
//            consumerFactory.close();
//            broker.stop();
//        }
//    }

//    @Test
//    public void testSendAndListen() throws Exception {
//        ConnectionContextFactory consumerFactory = null;
//        ConnectionContextFactory producerFactory = null;
//
//        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
//        broker.start();
//
//        try {
//            String brokerURL = broker.getBrokerURL();
//            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");
//
//            Map<String, String> details = new HashMap<String, String>();
//            details.put("key1", "val1");
//            details.put("secondkey", "secondval");
//            SpecificMessage specificMessage = new SpecificMessage("hello", details, "specific text");
//
//            // mimic server-side - this will receive the initial request message (and will send the response back)
//            consumerFactory = new ConnectionContextFactory(brokerURL);
//            ConsumerConnectionContext consumerContext = consumerFactory.createConsumerConnectionContext(endpoint);
//            TestRPCListener requestListener = new TestRPCListener();
//            MessageProcessor serverSideProcessor = new MessageProcessor();
//            serverSideProcessor.listen(consumerContext, requestListener);
//
//            // mimic client side - this will send the initial request message and receive the response from the server
//            producerFactory = new ConnectionContextFactory(brokerURL);
//            ProducerConnectionContext producerContext = producerFactory.createProducerConnectionContext(endpoint);
//            CountDownLatch latch = new CountDownLatch(1);
//            ArrayList<SpecificMessage> receivedMessages = new ArrayList<SpecificMessage>();
//            ArrayList<String> errors = new ArrayList<String>();
//            SpecificMessageStoreAndLatchListener responseListener = new SpecificMessageStoreAndLatchListener(latch,
//                    receivedMessages, errors);
//            MessageProcessor clientSideProcessor = new MessageProcessor();
//            clientSideProcessor.sendAndListen(producerContext, specificMessage, responseListener);
//
//            // wait for the message to flow
//            boolean gotMessage = latch.await(30, TimeUnit.SECONDS);
//            if (!gotMessage) {
//                errors.add("Timed out waiting for response message - it never showed up");
//            }
//
//            // make sure the message flowed properly
//            assertTrue("Failed to send message properly: " + errors, errors.isEmpty());
//            assertEquals("Didn't receive response: " + receivedMessages, receivedMessages.size(), 1);
//            SpecificMessage receivedSpecificMessage = receivedMessages.get(0);
//            assertEquals("RESPONSE:" + specificMessage.getMessage(), receivedSpecificMessage.getMessage());
//            assertEquals(specificMessage.getDetails(), receivedSpecificMessage.getDetails());
//            assertEquals("RESPONSE:" + specificMessage.getSpecific(), receivedSpecificMessage.getSpecific());
//
//        } finally {
//            // close everything
//            producerFactory.close();
//            consumerFactory.close();
//            broker.stop();
//        }
//    }

//    @Test
//    public void testRPCTimeout() throws Exception {
//        ConnectionContextFactory consumerFactory = null;
//        ConnectionContextFactory producerFactory = null;
//
//        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
//        broker.start();
//
//        try {
//            String brokerURL = broker.getBrokerURL();
//            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");
//
//            Map<String, String> details = new HashMap<String, String>();
//            details.put("key1", "val1");
//            details.put("secondkey", "secondval");
//            SpecificMessage specificMessage = new SpecificMessage("hello", details, "specific text");
//
//            // mimic server-side - this will receive the initial request message (and will send the response back)
//            consumerFactory = new ConnectionContextFactory(brokerURL);
//            ConsumerConnectionContext consumerContext = consumerFactory.createConsumerConnectionContext(endpoint);
//            TestRPCListener requestListener = new TestRPCListener(4000L); // wait so we have a chance to timeout
//            MessageProcessor serverSideProcessor = new MessageProcessor();
//            serverSideProcessor.listen(consumerContext, requestListener);
//
//            // mimic client side - this will send the initial request message and receive the response from the server
//            producerFactory = new ConnectionContextFactory(brokerURL);
//            ProducerConnectionContext producerContext = producerFactory.createProducerConnectionContext(endpoint);
//            MessageProcessor clientSideProcessor = new MessageProcessor();
//            ListenableFuture<BasicMessageWithExtraData<SpecificMessage>> future = clientSideProcessor.sendRPC(
//                    producerContext, specificMessage, SpecificMessage.class);
//
//            // wait for the message to flow - notice we don't wait long enough - this should timeout
//            BasicMessageWithExtraData<SpecificMessage> receivedSpecificMessage = null;
//            try {
//                receivedSpecificMessage = future.get(1, TimeUnit.SECONDS);
//                assert false : "Future failed to timeout; should have not got a response: " + receivedSpecificMessage;
//            } catch (TimeoutException expected) {
//                // expected
//            } catch (Exception e) {
//                assert false : "Future threw unexpected exception: " + e;
//            }
//
//            assertFalse(future.isCancelled());
//            assertFalse("Future should not have been done: " + future, future.isDone());
//
//            // ok, now wait for the message to flow
//            try {
//                receivedSpecificMessage = future.get();
//            } catch (Exception e) {
//                assert false : "Future failed to obtain response message: " + e;
//            }
//
//            // make sure the message flowed properly
//            assertNotNull("Didn't receive response", receivedSpecificMessage);
//            assertNotNull("Didn't receive response", receivedSpecificMessage.getBasicMessage());
//            assertEquals("RESPONSE:" + specificMessage.getMessage(), receivedSpecificMessage.getBasicMessage()
//                    .getMessage());
//            assertEquals(specificMessage.getDetails(), receivedSpecificMessage.getBasicMessage().getDetails());
//            assertEquals("RESPONSE:" + specificMessage.getSpecific(), receivedSpecificMessage.getBasicMessage()
//                    .getSpecific());
//            assertFalse(future.isCancelled());
//            assertTrue("Future should have been done: " + future, future.isDone());
//
//        } finally {
//            // close everything
//            producerFactory.close();
//            consumerFactory.close();
//            broker.stop();
//        }
//    }

//    @Test
//    public void testRPCCancel() throws Exception {
//        ConnectionContextFactory consumerFactory = null;
//        ConnectionContextFactory producerFactory = null;
//
//        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
//        broker.start();
//
//        try {
//            String brokerURL = broker.getBrokerURL();
//            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");
//
//            Map<String, String> details = new HashMap<String, String>();
//            details.put("key1", "val1");
//            details.put("secondkey", "secondval");
//            SpecificMessage specificMessage = new SpecificMessage("hello", details, "specific text");
//
//            // mimic server-side - this will receive the initial request message (and will send the response back)
//            consumerFactory = new ConnectionContextFactory(brokerURL);
//            ConsumerConnectionContext consumerContext = consumerFactory.createConsumerConnectionContext(endpoint);
//            TestRPCListener requestListener = new TestRPCListener(3000L); // wait so we have a chance to cancel it
//            MessageProcessor serverSideProcessor = new MessageProcessor();
//            serverSideProcessor.listen(consumerContext, requestListener);
//
//            // mimic client side - this will send the initial request message and receive the response from the server
//            producerFactory = new ConnectionContextFactory(brokerURL);
//            ProducerConnectionContext producerContext = producerFactory.createProducerConnectionContext(endpoint);
//            MessageProcessor clientSideProcessor = new MessageProcessor();
//            ListenableFuture<BasicMessageWithExtraData<SpecificMessage>> future = clientSideProcessor.sendRPC(
//                    producerContext, specificMessage, SpecificMessage.class);
//            TestFutureCallback futureCallback = new TestFutureCallback();
//            Futures.addCallback(future, futureCallback);
//
//            assertTrue("Failed to cancel the future", future.cancel(true));
//            assertTrue("Future should have been canceled: " + future, future.isCancelled());
//            assertTrue("Future should have been done: " + future, future.isDone());
//
//            // try to get the message using get(timeout) method
//            try {
//                future.get(30, TimeUnit.SECONDS);
//            } catch (CancellationException expected) {
//                // expected
//            } catch (Exception e) {
//                assert false : "Got unexpected exception: " + e;
//            }
//
//            // try to get the message using get() method
//            try {
//                future.get();
//            } catch (CancellationException expected) {
//                // expected
//            } catch (Exception e) {
//                assert false : "Got unexpected exception: " + e;
//            }
//
//            // try to get the message using the future callback
//            try {
//                futureCallback.getResult(30, TimeUnit.SECONDS);
//            } catch (CancellationException expected) {
//                // expected
//            } catch (Throwable t) {
//                assert false : "Got unexpected exception: " + t;
//            }
//
//        } finally {
//            // close everything
//            producerFactory.close();
//            consumerFactory.close();
//            broker.stop();
//        }
//    }

    private class TestRPCListener extends RPCBasicMessageListener<SpecificMessage, SpecificMessage> {
        private long sleep; // amount of seconds the onBasicMessage will sleep before returning the response

        public TestRPCListener() {
            sleep = 0L;
        }

        public TestRPCListener(long sleep) {
            this.sleep = sleep;
        }

        @Override
        protected SpecificMessage onBasicMessage(BasicMessageWithExtraData<SpecificMessage> requestMessageWithData) {
            if (this.sleep > 0L) {
                try {
                    Thread.sleep(this.sleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            SpecificMessage reqMsg = requestMessageWithData.getBasicMessage();
            SpecificMessage responseMessage = new SpecificMessage("RESPONSE:" + reqMsg.getMessage(),
                    reqMsg.getDetails(), "RESPONSE:" + reqMsg.getSpecific());
            return responseMessage;
        }
    }

//    private class TestFutureCallback implements FutureCallback<BasicMessageWithExtraData<SpecificMessage>> {
//        private final CountDownLatch latch = new CountDownLatch(1);
//        private BasicMessageWithExtraData<SpecificMessage> msg = null;
//        private Throwable error = null;
//
//        @Override
//        public void onSuccess(BasicMessageWithExtraData<SpecificMessage> result) {
//            msg = result;
//            latch.countDown();
//        }
//
//        @Override
//        public void onFailure(Throwable t) {
//            error = (t != null) ? t : new Exception("unknown throwable occurred");
//            latch.countDown();
//        }
//
//        public BasicMessageWithExtraData<SpecificMessage> getResult(long waitTime, TimeUnit timeUnit) throws Throwable {
//            try {
//                latch.await(waitTime, timeUnit);
//            } catch (Exception e) {
//                assert false : "Did not get a timely response";
//            }
//            if (error != null) {
//                throw error;
//            }
//            return msg;
//        }
//    }
}
