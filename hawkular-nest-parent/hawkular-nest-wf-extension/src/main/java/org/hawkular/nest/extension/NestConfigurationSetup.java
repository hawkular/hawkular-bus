package org.hawkular.nest.extension;

import java.util.Map;

import org.jboss.as.server.ServerEnvironment;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;

public class NestConfigurationSetup {

    private final Logger log = Logger.getLogger(NestConfigurationSetup.class);

    /**
     * Properties that will be used to complete the out-of-box configuration.
     */
    private final Map<String, String> customConfiguration;

    /**
     * Provides environment information about the server in which we are embedded.
     */
    private final ServerEnvironment serverEnvironment;

    public NestConfigurationSetup(Map<String, String> customConfigProps, ServerEnvironment serverEnv) {
        this.customConfiguration = customConfigProps;
        this.serverEnvironment = serverEnv;
        prepareConfiguration();
    }

    public Map<String, String> getCustomConfiguration() {
        return customConfiguration;
    }

    public ServerEnvironment getServerEnvironment() {
        return serverEnvironment;
    }

    private void prepareConfiguration() {
        // replace ${x} tokens in all values
        for (Map.Entry<String, String> entry : this.customConfiguration.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                entry.setValue(StringPropertyReplacer.replaceProperties(value));
            }
        }

        log.debug("configuration: [" + this.customConfiguration + "]");
        return;
    }
}
