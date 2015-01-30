/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.bus.broker.extension;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.hawkular.bus.broker.EmbeddedBroker;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class BrokerService implements Service<BrokerService> {

    public static final ServiceName SERVICE_NAME = ServiceName.of("org.hawkular.bus").append(
            BrokerSubsystemExtension.SUBSYSTEM_NAME);

    private final Logger log = Logger.getLogger(BrokerService.class);

    /**
     * Our subsystem add-step handler will inject this as a dependency for us. This service gives us information about
     * the server, like the install directory, data directory, etc. Package-scoped so the add-step handler can access
     * this.
     */
    final InjectedValue<ServerEnvironment> envServiceValue = new InjectedValue<ServerEnvironment>();

    /**
     * Our subsystem add-step handler will inject this as a dependency for us. This object will provide the binding
     * address and port for the broker's transport connector.
     */
    final InjectedValue<SocketBinding> connectorSocketBinding = new InjectedValue<SocketBinding>();

    /**
     * Our subsystem add-step handler will inject this as a dependency for us. This object will provide the multicast
     * address and port for the broker's network connector which is used to discover other brokers.
     */
    final InjectedValue<SocketBinding> discoverySocketBinding = new InjectedValue<SocketBinding>();

    /**
     * The broker configuration file that is used to completely configure the broker. This is the "out-of-box"
     * configuration that can be customized with overrides via {@link #customConfigProperties}.
     */
    private String configurationFile;

    /**
     * Configuration settings that help complete the out-of-box configuration file. These are settings that the user set
     * in the subsystem (e.g. standalone.xml or via AS CLI).
     */
    private Map<String, String> customConfigProperties = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * This is the actual embedded broker.
     */
    private AtomicReference<EmbeddedBroker> theBroker = new AtomicReference<EmbeddedBroker>();

    /**
     * This is the daemon thread running the broker.
     */
    private Thread brokerThread;

    public BrokerService() {
    }

    @Override
    public BrokerService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.info("Broker service starting");
        startBroker();
        log.info("Broker service started");
    }

    @Override
    public void stop(StopContext context) {
        log.info("Broker service stopping");
        stopBroker();
        log.info("Broker service stopped");
    }

    protected void setConfigurationFile(String configFile) {
        this.configurationFile = configFile;
    }

    protected void setCustomConfigurationProperties(Map<String, String> properties) {
        synchronized (customConfigProperties) {
            customConfigProperties.clear();
            if (properties != null) {
                customConfigProperties.putAll(properties);
            }
        }
    }

    public boolean isBrokerStarted() {
        EmbeddedBroker broker = theBroker.get();
        return (broker != null && broker.isBrokerStarted());
    }

    public String getBrokerName() {
        return this.customConfigProperties.get(BrokerSubsystemExtension.BROKER_NAME_SYSPROP);
    }

    protected void startBroker() throws StartException {
        if (isBrokerStarted()) {
            log.info("Broker is already started.");
            return;
        }

        log.info("Starting the broker now");
        try {
            // make sure we pre-configure the broker with some settings taken from our runtime environment

            // get the socket the transport connector is to bind to - make sure we do not bind "to all"
            SocketBinding connectorSocketBindingValue = connectorSocketBinding.getValue();
            String connectorAddress = connectorSocketBindingValue.getAddress().getHostAddress();
            String connectorPort = String.valueOf(connectorSocketBindingValue.getAbsolutePort());
            if (connectorAddress.equals("0.0.0.0") || connectorAddress.equals("::/128")) {
                connectorAddress = InetAddress.getLocalHost().getCanonicalHostName();
            }

            customConfigProperties.put(BrokerSubsystemExtension.BROKER_CONNECTOR_ADDRESS_SYSPROP, connectorAddress);
            customConfigProperties.put(BrokerSubsystemExtension.BROKER_CONNECTOR_PORT_SYSPROP, connectorPort);
            log.info("Broker told to bind socket to [" + connectorAddress + ":" + connectorPort + "]");

            SocketBinding discoverySocketBindingValue = discoverySocketBinding.getValue();
            String discoveryAddress = discoverySocketBindingValue.getMulticastAddress().getHostAddress();
            String discoveryPort = String.valueOf(discoverySocketBindingValue.getMulticastPort());
            customConfigProperties.put(BrokerSubsystemExtension.BROKER_DISCOVERY_ADDRESS_SYSPROP, discoveryAddress);
            customConfigProperties.put(BrokerSubsystemExtension.BROKER_DISCOVERY_PORT_SYSPROP, discoveryPort);
            log.info("Broker told to discover other brokers via [" + discoveryAddress + ":" + discoveryPort + "]");

            ServerEnvironment env = envServiceValue.getValue();
            BrokerConfigurationSetup configSetup = new BrokerConfigurationSetup(configurationFile,
                    customConfigProperties, env);
            log.info("Broker told to use configuration file [" + configSetup.getConfigurationFile() + "]");

            // build the startup command line arguments to pass to the broker
            Map<String, String> customConfig = configSetup.getCustomConfiguration();
            String[] args = new String[(2 * customConfig.size()) + 2];
            int argIndex = 0;
            for (Map.Entry<String, String> entry : customConfig.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                args[argIndex++] = "-D";
                args[argIndex++] = name + "=" + value;
            }
            args[argIndex++] = "-c";
            args[argIndex++] = configSetup.getConfigurationFile();

            theBroker.set(new EmbeddedBroker(args));

            brokerThread = new Thread("Broker Start Thread") {
                public void run() {
                    try {
                        theBroker.get().startBroker();
                    } catch (InterruptedException e) {
                        // broker just exited due to being shutdown, die quietly
                        log.debug("Broker has exited.");
                    } catch (Throwable t) {
                        log.error("Broker aborted with exception.", t);
                    }
                };
            };
            brokerThread.setDaemon(true);
            brokerThread.start();
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    protected void stopBroker() {
        try {
            if (!isBrokerStarted()) {
                log.info("Broker is already stopped.");
            } else {
                log.info("Stopping the broker now");
                theBroker.get().stopBroker();
            }
        } catch (Throwable t) {
            log.error("Failed to shutdown broker", t);
        } finally {
            if (brokerThread != null) {
                brokerThread.interrupt();
            }
        }
        theBroker.set(null);
    }
}
