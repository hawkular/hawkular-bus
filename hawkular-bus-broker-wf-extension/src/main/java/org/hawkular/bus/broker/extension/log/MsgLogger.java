/*
 * Copyright 2014-2015 Red Hat, Inc. and/or its affiliates and other contributors as indicated by the @author tags.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.hawkular.bus.broker.extension.log;

import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author John Mazzitelli
 */
@MessageLogger(projectCode = "HAWK")
@ValidIdRange(min = 101000, max = 101999)
public interface MsgLogger {
    @LogMessage(level = Level.WARN)
    @Message(id = 101000, value = "Cannot determine absolute path of config file [%s]. Does it exist? - %s")
    void warnCannotDetermineConfigFilePath(String configFile, String causeString);

    @LogMessage(level = Level.INFO)
    @Message(id = 101001, value = "Broker Service Starting")
    void infoBrokerServiceStarting();

    @LogMessage(level = Level.INFO)
    @Message(id = 101002, value = "Broker Service Started")
    void infoBrokerServiceStarted();

    @LogMessage(level = Level.INFO)
    @Message(id = 101003, value = "Broker Service Stopping")
    void infoBrokerServiceStopping();

    @LogMessage(level = Level.INFO)
    @Message(id = 101004, value = "Broker Service Stopped")
    void infoBrokerServiceStopped();

    @LogMessage(level = Level.INFO)
    @Message(id = 101005, value = "Broker is already started")
    void infoBrokerAlreadyStarted();

    @LogMessage(level = Level.INFO)
    @Message(id = 101006, value = "Starting the broker now")
    void infoStartingBrokerNow();

    @LogMessage(level = Level.INFO)
    @Message(id = 101007, value = "Broker told to bind socket to [%s:%s]")
    void infoBrokerBindingToSocket(String connectorAddress, String connectorPort);

    @LogMessage(level = Level.INFO)
    @Message(id = 101008, value = "Broker told to discover other brokers via [%s:%s]")
    void infoBrokerDiscoveryEndpoint(String discoveryAddress, String discoveryPort);

    @LogMessage(level = Level.INFO)
    @Message(id = 101009, value = "Broker told to use configuration file [%s]")
    void infoBrokerConfigurationFile(String configFile);

    @LogMessage(level = Level.ERROR)
    @Message(id = 101010, value = "Broker aborted with exception.")
    void errorBrokerAborted(@Cause Throwable t);

    @LogMessage(level = Level.INFO)
    @Message(id = 101011, value = "Broker is already stopped.")
    void infoBrokerAlreadyStopped();

    @LogMessage(level = Level.INFO)
    @Message(id = 101012, value = "Stopping the broker now")
    void infoStoppingBrokerNow();

    @LogMessage(level = Level.ERROR)
    @Message(id = 101013, value = "Failed to shutdown broker")
    void errorFailedToShutdownBroker(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 101014, value = "The REST WAR could not be deployed; REST interface to the broker is unavailable: %s")
    void errorRestWarCouldNotBeDeployed(String causeString);

    @LogMessage(level = Level.INFO)
    @Message(id = 101015, value = "Broker is not enabled and will not be deployed")
    void infoBrokerNotEnabled();

    @LogMessage(level = Level.INFO)
    @Message(id = 101016, value = "Broker is enabled and will be deployed using config file [%s]")
    void infoBrokerEnabledWithConfigFile(String configFile);

    @LogMessage(level = Level.INFO)
    @Message(id = 101017, value = "Initializing broker subsystem")
    void infoInitializingBrokerSubsystem();

    @LogMessage(level = Level.INFO)
    @Message(id = 101018, value = "Asked to restart the broker. Will stop it, then restart it now.")
    void infoAskedToRestartBroker();

    @LogMessage(level = Level.INFO)
    @Message(id = 101019, value = "Asked to stop the broker.")
    void infoAskedToStopBroker();
}
