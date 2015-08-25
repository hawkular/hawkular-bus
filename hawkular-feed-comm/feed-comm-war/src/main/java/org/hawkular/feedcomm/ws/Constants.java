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

import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.Endpoint.Type;
import org.hawkular.feedcomm.ws.command.EchoCommand;
import org.hawkular.feedcomm.ws.command.GenericErrorResponseCommand;
import org.hawkular.feedcomm.ws.command.feed.DeployApplicationResponseCommand;
import org.hawkular.feedcomm.ws.command.feed.ExecuteOperationResponseCommand;
import org.hawkular.feedcomm.ws.command.ui.AddJdbcDriverCommand;
import org.hawkular.feedcomm.ws.command.ui.DeployApplicationCommand;
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
     * A JMS message header that will identify the targeted UI client.
     */
    String HEADER_UICLIENTID = "uiClientId";

    /**
     * The JNDI name of the bus connection factory.
     */
    String CONNECTION_FACTORY_JNDI = "java:/HawkularBusConnectionFactory";


    /**
     * These are the only valid commands that can come from feeds.
     */
    ValidCommandsMap VALID_COMMANDS_FROM_FEED = new ValidCommandsMap()
            .put(EchoCommand.REQUEST_CLASS.getName(), EchoCommand.class)
            .put(ExecuteOperationResponseCommand.REQUEST_CLASS.getName(), ExecuteOperationResponseCommand.class)
            .put(DeployApplicationResponseCommand.REQUEST_CLASS.getName(), DeployApplicationResponseCommand.class)
            .put(GenericErrorResponseCommand.REQUEST_CLASS.getName(), GenericErrorResponseCommand.class);

    /**
     * These are the only valid commands that can come from UI clients.
     */
    ValidCommandsMap VALID_COMMANDS_FROM_UI = new ValidCommandsMap()
            .put(EchoCommand.REQUEST_CLASS.getName(), EchoCommand.class)
            .put(DeployApplicationCommand.REQUEST_CLASS.getName(), DeployApplicationCommand.class)
            .put(AddJdbcDriverCommand.REQUEST_CLASS.getName(), AddJdbcDriverCommand.class)
            .put(ExecuteOperationCommand.REQUEST_CLASS.getName(), ExecuteOperationCommand.class)
            .put(GenericErrorResponseCommand.REQUEST_CLASS.getName(), GenericErrorResponseCommand.class);

    // QUEUES AND TOPICS
    Endpoint DEST_FEED_EXECUTE_OP = new Endpoint(Type.QUEUE, "FeedExecuteOperation");
    Endpoint DEST_FEED_DEPLOY_APPLICATION = new Endpoint(Type.QUEUE, "FeedDeployApplication");
    Endpoint DEST_FEED_ADD_JDBC_DRIVER = new Endpoint(Type.QUEUE, "FeedAddJdbcDriver");

    Endpoint DEST_UICLIENT_EXECUTE_OP_RESPONSE = new Endpoint(Type.QUEUE, "UIClientExecuteOperationResponse");
    Endpoint DEST_UICLIENT_DEPLOY_APPLICATION_RESPONSE = new Endpoint(Type.QUEUE, "UIDeployApplicationResponse");
    Endpoint DEST_UICLIENT_ADD_JDBC_DRIVER_RESPONSE = new Endpoint(Type.QUEUE, "UIAddJdbcDriverResponse");

}
