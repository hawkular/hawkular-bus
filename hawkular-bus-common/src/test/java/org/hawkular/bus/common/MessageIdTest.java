package org.hawkular.bus.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;


public class MessageIdTest {
    @Test
    public void messageIdEquality() {
        MessageId one = new MessageId("msg1");
        MessageId oneDup = new MessageId("msg1");
        MessageId two = new MessageId("msg2");
        assertEquals(one, oneDup);
        assertFalse(one.equals(two));
    }
}
