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

import org.hawkular.feedcomm.ws.command.EchoCommand;
import org.hawkular.feedcomm.ws.command.GenericErrorResponseCommand;
import org.hawkular.feedcomm.ws.command.feed.ExecuteOperationResponseCommand;
import org.hawkular.feedcomm.ws.command.ui.ExecuteOperationCommand;
import org.hawkular.feedcomm.ws.server.ValidCommandsMap;

/**
 * Global constants.
 */
public interface Constants {
    /**
     * A JMS message header that will identify the targeted feed.
     */
    String HEADER_FEEDID = "feedId";

    /**
     * The JNDI name of the bus connection factory.
     */
    String CONNECTION_FACTORY_JNDI = "java:/HawkularBusConnectionFactory";


    /**
     * These are the only valid commands that can come from feeds.
     */
    ValidCommandsMap VALID_COMMANDS_FROM_FEED = new ValidCommandsMap()
            .put(EchoCommand.REQUEST_CLASS.getName(), EchoCommand.class)
            .put(GenericErrorResponseCommand.REQUEST_CLASS.getName(), GenericErrorResponseCommand.class)
            .put(ExecuteOperationResponseCommand.REQUEST_CLASS.getName(), ExecuteOperationResponseCommand.class);

    /**
     * These are the only valid commands that can come from UI clients.
     */
    ValidCommandsMap VALID_COMMANDS_FROM_UI = new ValidCommandsMap()
            .put(EchoCommand.REQUEST_CLASS.getName(), EchoCommand.class)
            .put(GenericErrorResponseCommand.REQUEST_CLASS.getName(), GenericErrorResponseCommand.class)
            .put(ExecuteOperationCommand.REQUEST_CLASS.getName(), ExecuteOperationCommand.class);

    // QUEUES AND TOPICS
    String DEST_FEED_EXECUTE_OP = "FeedExecuteOperation";

}
