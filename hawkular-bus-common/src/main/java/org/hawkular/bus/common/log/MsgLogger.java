/*
 * Copyright 2014-2015 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.bus.common.log;

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
@ValidIdRange(min = 100000, max = 100999)
public interface MsgLogger {
    @LogMessage(level = Level.ERROR)
    @Message(id = 100000, value = "A message was received that was not a valid text message")
    void errorNotValidTextMessage(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 100001, value = "A message was received that was not a valid JSON-encoded BasicMessage object")
    void errorNotValidJsonMessage(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 100002, value = "Cannot close the previous connection; memory might leak.")
    void errorCannotCloseConnectionMemoryMightLeak(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 100003, value = "Failed to start connection.")
    void errorFailedToStartConnection(@Cause Throwable t);
}
