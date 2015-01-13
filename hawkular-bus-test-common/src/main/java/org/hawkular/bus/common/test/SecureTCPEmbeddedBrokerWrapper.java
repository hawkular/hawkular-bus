package org.hawkular.bus.common.test;


/**
 * Used to start a simple test broker that has security enabled (that is, requires users to log in to access the broker).
 */
public class SecureTCPEmbeddedBrokerWrapper extends TCPEmbeddedBrokerWrapper {

    public SecureTCPEmbeddedBrokerWrapper() throws Exception {
        super();
    }

    @Override
    protected String getConfigurationFile() {
        return "secure-simple-activemq.xml";
    }
}
