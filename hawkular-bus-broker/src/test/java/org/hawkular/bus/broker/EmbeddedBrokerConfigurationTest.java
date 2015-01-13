package org.hawkular.bus.broker;

import org.junit.Test;

public class EmbeddedBrokerConfigurationTest {
    @Test
    public void testPropertiesConfig() throws Exception {
        new EmbeddedBroker(new String[] { "--config=test-broker.properties" }).stopBroker();
    }

    @Test
    public void testXMLConfig() throws Exception {
        new EmbeddedBroker(new String[] { "--config=test-broker.xml", "-Dtest.bind.port=61616" }).stopBroker();
    }
}
