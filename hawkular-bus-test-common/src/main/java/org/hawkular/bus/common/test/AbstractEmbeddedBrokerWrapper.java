package org.hawkular.bus.common.test;

import org.hawkular.bus.broker.EmbeddedBroker;

/**
 * Wrapper around an embedded broker. Subclasses provide concrete
 * implementations using different connectors (e.g. a in-memory broker or a
 * broker listening on a TCP port).
 */
public abstract class AbstractEmbeddedBrokerWrapper {
    private EmbeddedBroker broker;

    public void setBroker(EmbeddedBroker b) {
        if (b == null) {
            throw new NullPointerException("broker must not be null");
        }
        broker = b;
    }

    public EmbeddedBroker getBroker() {
        return broker;
    }

    public void start() throws Exception {
        getBroker().startBroker();
    }

    public void stop() throws Exception {
        getBroker().stopBroker();
    }

    public abstract String getBrokerURL();
}
