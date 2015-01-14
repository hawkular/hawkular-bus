package org.hawkular.nest.extension;

import java.io.IOException;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(NestSubsystemExtension.SUBSYSTEM_NAME, new NestSubsystemExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return readResource("subsystem.xml");
    }
}
