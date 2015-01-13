package org.hawkular.bus.broker.extension;

import java.io.IOException;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(BrokerSubsystemExtension.SUBSYSTEM_NAME, new BrokerSubsystemExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return readResource("subsystem.xml");
    }
}
