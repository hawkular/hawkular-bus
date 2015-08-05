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
package org.hawkular.feedcomm.ws;

import javax.websocket.CloseReason;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

@MessageLogger(projectCode = "HAWKFEEDCOMM")
@ValidIdRange(min = 1, max = 5000)
public interface MsgLogger extends BasicLogger {

    MsgLogger LOG = Logger.getMessageLogger(MsgLogger.class, "org.hawkular.feedcomm.ws");

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 1, value = "Feed [%s] provided an invalid command request: [%s]")
    void errorInvalidCommandRequestFeed(String feedId, String invalidCommandRequest);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 2, value = "Failed to execute command [%s] for feed [%s]")
    void errorCommandExecutionFailureFeed(String commandRequest, String feedId, @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 3, value = "A feed [%s] opened multiple sessions. This is a violation; closing the extra session")
    void errorClosingExtraFeedSession(String feedId);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 4, value = "Cannot close the extra session created by feed [%s]")
    void errorCannotCloseExtraFeedSession(String feedId, @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 5, value = "UI client [%s] (session [%s]) provided an invalid command request: [%s]")
    void errorInvalidCommandRequestUIClient(String uiClientId, String sessionId, String invalidCommandRequest);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 6, value = "Failed to execute command [%s] for UI client [%s] (session [%s])")
    void errorCommandExecutionFailureUIClient(String commandRequest, String uiClientId, String sessionId,
            @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 7, value = "Cannot process an execute-operation message")
    void errorCannotProcessExecuteOperationMessage(@Cause Throwable t);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 8, value = "Received the following error message and stack trace from remote endpoint: %s\n%s")
    void warnReceivedGenericErrorResponse(String errorMessage, String stackTrack);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 9, value = "Feed [%s] session opened [%s]")
    void infoFeedSessionOpened(String feedId, String sessionId);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 10, value = "Failed to add message listeners for feed [%s]. Closing session [%s]")
    void errorFailedToAddMessageListenersForFeed(String feedId, String id, @Cause Throwable t);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 11, value = "Received message from feed [%s]")
    void infoReceivedMessageFromFeed(String feedId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 12, value = "Feed [%s] session closed. Reason=[%s]")
    void infoFeedSessionClosed(String feedId, CloseReason reason);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 13, value = "UI client session [%s] opened")
    void infoUIClientSessionOpened(String id);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 14, value = "Received message from UI client [%s] (session [%s])")
    void infoReceivedMessageFromUIClient(String uiClientId, String sessionId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 15, value = "UI client [%s] (session [%s]) closed. Reason=[%s]")
    void infoUISessionClosed(String uiClientId, String sessionId, CloseReason reason);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 16, value = "Adding listeners for feed [%s]")
    void infoAddingListenersForFeed(String feedId);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 17, value = "Failed to close consumer context; will keep trying to close the rest")
    void errorFailedClosingConsumerContext(@Cause Throwable t);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 18, value = "Removing listeners for feed [%s]")
    void infoRemovingListenersForFeed(String feedId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 19, value = "Failed to removing listeners for feed [%s]")
    void errorFailedRemovingListenersForFeed(String feedId, @Cause Throwable t);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 20, value = "Received binary data from feed [%s]")
    void infoReceivedBinaryDataFromFeed(String feedId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 21, value = "Received binary data from UI client [%s]")
    void infoReceivedBinaryDataFromUIClient(String id);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 22, value = "Adding listeners for UI client [%s]")
    void infoAddingListenersForUIClient(String uiClientId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 23, value = "Removing listeners for UI client [%s]")
    void infoRemovingListenersForUIClient(String uiClientId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 24, value = "Failed to removing listeners for UI client [%s]")
    void errorFailedRemovingListenersForUIClient(String uiClientId, @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 25, value = "Failed to add message listeners for UI client [%s]. Closing session [%s]")
    void errorFailedToAddMessageListenersForUIClient(String uiClientId, String sessionId, @Cause Throwable t);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 26, value = "Cannot process an execute-operation-response message")
    void errorCannotProcessExecuteOperationResponseMessage(@Cause Throwable t);
}
