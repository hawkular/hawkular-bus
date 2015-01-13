package org.hawkular.bus.common.test;

import org.hawkular.bus.broker.EmbeddedBroker;

/**
 * Used to start a simple test broker running intra-VM (no TCP connections accepted).
 */
public class VMEmbeddedBrokerWrapper extends AbstractEmbeddedBrokerWrapper {

    public VMEmbeddedBrokerWrapper() throws Exception {
        setBroker(new EmbeddedBroker(new String[] { "--config=simple-activemq.properties" }));
    }

    @Override
    public String getBrokerURL() {
        return "vm://simple-testbroker?create=false";
    }
}
