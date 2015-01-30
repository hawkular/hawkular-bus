package org.hawkular.bus.broker.extension;

import java.io.File;
import java.util.Map;

import org.jboss.as.server.ServerEnvironment;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.Resource;
import org.jboss.util.StringPropertyReplacer;

public class BrokerConfigurationSetup {

    private final Logger log = Logger.getLogger(BrokerConfigurationSetup.class);

    /**
     * The location of the configuration file. This will be a usable path for the broker to use.
     */
    private final String configurationFile;

    /**
     * Properties that will be used to complete the out-of-box configuration.
     */
    private final Map<String, String> customConfiguration;

    /**
     * Provides environment information about the server in which we are embedded.
     */
    private final ServerEnvironment serverEnvironment;

    public BrokerConfigurationSetup(String configFile, Map<String, String> customConfigProps,
            ServerEnvironment serverEnv) {
        if (configFile == null || configFile.trim().isEmpty()) {
            configFile = BrokerSubsystemExtension.BROKER_CONFIG_FILE_DEFAULT;
        }
        this.customConfiguration = customConfigProps;
        this.serverEnvironment = serverEnv;
        this.configurationFile = getUsableConfigurationFilePath(configFile, serverEnv);
        prepareConfiguration();
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public Map<String, String> getCustomConfiguration() {
        return customConfiguration;
    }

    public ServerEnvironment getServerEnvironment() {
        return serverEnvironment;
    }

    private void prepareConfiguration() {
        // perform some checking to setup defaults if need be
        Map<String, String> customConfigProps = this.customConfiguration;
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_NAME_SYSPROP, //
                BrokerSubsystemExtension.BROKER_NAME_DEFAULT);
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_PERSISTENT_SYSPROP, //
                Boolean.toString(BrokerSubsystemExtension.PERSISTENT_DEFAULT));
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_USE_JMX_SYSPROP, //
                Boolean.toString(BrokerSubsystemExtension.USE_JMX_DEFAULT));
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_CONNECTOR_NAME_SYSPROP, //
                BrokerSubsystemExtension.CONNECTOR_NAME_DEFAULT);
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_CONNECTOR_PROTOCOL_SYSPROP, //
                BrokerSubsystemExtension.CONNECTOR_PROTOCOL_DEFAULT);

        // replace ${x} tokens in all values
        for (Map.Entry<String, String> entry : customConfigProps.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                entry.setValue(StringPropertyReplacer.replaceProperties(value));
            }
        }
        return;
    }

    private void prepareConfigurationProperty(Map<String, String> customConfigProps, String prop, String defaultValue) {
        String propValue = customConfigProps.get(prop);
        if (propValue == null || propValue.trim().length() == 0 || "-".equals(propValue)) {
            log.debug("Broker configuration property [" + prop + "] was undefined; will default to [" + defaultValue
                    + "]");
            customConfigProps.put(prop, defaultValue);
        }
        return;
    }

    /**
     * Because the EmbeddedBroker uses third party libs to read the config file, it needs to have been put it in a place
     * where we can know and pass along its absolute path. This returns that absolute path of the config file.
     *
     * @param configFile
     *            the absolute or relative path that will be converted to absolute path the broker can use
     * @param serverEnv
     *            the server environment we can use to look for the file
     *
     * @return the absolute path of the config file that the broker will use
     */
    private String getUsableConfigurationFilePath(String configFile, ServerEnvironment serverEnv) {
        File file = new File(configFile);

        // if it is already absolute, use it as-is
        if (file.isAbsolute()) {
            return file.getAbsolutePath();
        }

        // see if there is one in the server configuration directory; if so, use it.
        File serverConfigDir = serverEnv.getServerConfigurationDir();
        File configFileInServerConfigDir = new File(serverConfigDir, configFile);
        if (configFileInServerConfigDir.exists()) {
            return configFileInServerConfigDir.getAbsolutePath();
        }

        // we still can't find the config file - see if its in the module's exported config/ directory
        try {
            Module module = Module.forClass(getClass());
            Resource r = module.getExportedResource("config", configFile);
            return r.getURL().toString();
        } catch (Throwable t) {
            // oh well, we tried - return the configFile as-is - we'll probably fail later because its probably missing
            log.info("Cannot determine absolute path of config file [" + configFile + "]- does it exist? - "
                    + t.toString());
            return configFile;
        }
    }
}