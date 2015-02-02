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
package org.hawkular.bus.broker.log;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author John Mazzitelli
 */
@MessageLogger(projectCode = "HAWKBUS")
@ValidIdRange(min = 120000, max = 129999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Level.INFO)
    @Message(id = 120000, value = "Attempting to start the broker")
    void infoStartingBroker();

    @LogMessage(level = Level.INFO)
    @Message(id = 120001, value = "Started broker")
    void infoStartedBroker();

    @LogMessage(level = Level.INFO)
    @Message(id = 120002, value = "Attempting to stop the broker")
    void infoStoppingBroker();

    @LogMessage(level = Level.INFO)
    @Message(id = 120003, value = "Stopped broker")
    void infoStoppedBroker();

    @LogMessage(level = Level.INFO)
    @Message(id = 120004, value = "Initialized broker")
    void infoInitializedBroker();

    @LogMessage(level = Level.INFO)
    @Message(id = 120005, value = "%s")
    void infoUsage(String usageString);
}
