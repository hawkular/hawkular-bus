package org.hawkular.bus.common.test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * This is testing the ability to subclass a generic listener superclass and have the JSON decoding work properly by
 * simply using reflection to determine the class of T (in this test case, T is SpecificMessage).
 */
public class SpecificMessageStoreAndLatchListener extends StoreAndLatchBasicMessageListener<SpecificMessage> {
    public SpecificMessageStoreAndLatchListener(CountDownLatch latch, ArrayList<SpecificMessage> messages,
            ArrayList<String> errors) {
        super(latch, messages, errors, SpecificMessage.class);
    }
}
