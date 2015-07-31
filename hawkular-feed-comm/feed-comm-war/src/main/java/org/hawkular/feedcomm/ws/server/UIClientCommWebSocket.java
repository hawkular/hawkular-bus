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

package org.hawkular.feedcomm.ws.server;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.feedcomm.api.ApiDeserializer;
import org.hawkular.feedcomm.api.GenericErrorResponseBuilder;
import org.hawkular.feedcomm.ws.Constants;
import org.hawkular.feedcomm.ws.MsgLogger;
import org.hawkular.feedcomm.ws.command.BinaryData;
import org.hawkular.feedcomm.ws.command.Command;
import org.hawkular.feedcomm.ws.command.CommandContext;

/**
 * This is similiar to the feed web socket endpoint, however, it has a different set of allowed commants
 * that can be processed for a UI client.
 */
@ServerEndpoint("/ui/ws")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UIClientCommWebSocket {

    @Inject
    private ConnectedFeeds connectedFeeds;

    @Inject
    private ConnectedUIClients connectedUIClients;

    @Resource(mappedName = Constants.CONNECTION_FACTORY_JNDI)
    private ConnectionFactory connectionFactory;

    // I don't know why @Resource injection doesn't work, but this is a backup.
    // We can get rid of this once we figure out what's broken and fix it.
    @PostConstruct
    public void lookupConnectionFactory() throws Exception {
        if (this.connectionFactory == null) {
            MsgLogger.LOG.warnf("Injection of ConnectionFactory is not working - looking it up explicitly");
            InitialContext ctx = new InitialContext();
            this.connectionFactory = (ConnectionFactory) ctx.lookup(Constants.CONNECTION_FACTORY_JNDI);
        } else {
            MsgLogger.LOG.warnf("Injection of ConnectionFactory works - you can remove the hack");
        }
    }

    @OnOpen
    public void uiClientSessionOpen(Session session) {
        MsgLogger.LOG.infoUIClientSessionOpened(session.getId());
        connectedUIClients.addSession(session);
    }

    /**
     * When a message is received from a UI client, this method will execute the command the client is asking for.
     *
     * @param nameAndJsonStr the name of the API request followed by "=" followed then by the request's JSON data
     * @param session the client session making the request
     * @return the results of the command invocation; this is sent back to the UI client
     */
    @OnMessage
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String uiClientMessage(String nameAndJsonStr, Session session) {

        MsgLogger.LOG.infoReceivedMessageFromUI(session.getId());

        String requestClassName = "?";
        BasicMessage response;

        try {
            BasicMessage request = new ApiDeserializer().deserialize(nameAndJsonStr);
            requestClassName = request.getClass().getName();

            Class<? extends Command<?, ?>> commandClass = Constants.VALID_COMMANDS_FROM_UI.get(requestClassName);
            if (commandClass == null) {
                MsgLogger.LOG.errorInvalidCommandRequestUIClient(session.getId(), requestClassName);
                String errorMessage = "Invalid command request: " + requestClassName;
                response = new GenericErrorResponseBuilder().setErrorMessage(errorMessage).build();
            } else {
                CommandContext context = new CommandContext(connectedFeeds, connectedUIClients, connectionFactory);
                Command command = commandClass.newInstance();
                response = command.execute(request, null, context);
            }
        } catch (Throwable t) {
            MsgLogger.LOG.errorCommandExecutionFailureUIClient(requestClassName, session.getId(), t);
            String errorMessage = "Command failed [" + requestClassName + "]";
            response = new GenericErrorResponseBuilder()
                    .setThrowable(t)
                    .setErrorMessage(errorMessage)
                    .build();

        }

        String responseText = (response == null) ? null : ApiDeserializer.toHawkularFormat(response);
        return responseText;
    }

    /**
     * When a binary message is received from a UI client, this method will execute the command the client
     * is asking for.
     *
     * @param binaryDataStream contains the JSON request and additional binary data
     * @param session the client session making the request
     * @return the results of the command invocation; this is sent back to the UI client
     */
    @OnMessage
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String uiClientBinaryData(InputStream binaryDataStream, Session session) {
        MsgLogger.LOG.infoReceivedBinaryDataFromUI(session.getId());

        String requestClassName = "?";
        BasicMessage response;

        try {
            Map<BasicMessage, byte[]> requestMap = new ApiDeserializer().deserialize(binaryDataStream);
            BasicMessage request = requestMap.keySet().iterator().next();
            byte[] inMemoryData = requestMap.values().iterator().next();
            BinaryData binaryData = new BinaryData(inMemoryData, binaryDataStream);
            requestClassName = request.getClass().getName();

            Class<? extends Command<?, ?>> commandClass = Constants.VALID_COMMANDS_FROM_UI.get(requestClassName);
            if (commandClass == null) {
                MsgLogger.LOG.errorInvalidCommandRequestUIClient(session.getId(), requestClassName);
                String errorMessage = "Invalid command request: " + requestClassName;
                response = new GenericErrorResponseBuilder().setErrorMessage(errorMessage).build();
            } else {
                CommandContext context = new CommandContext(connectedFeeds, connectedUIClients, connectionFactory);
                Command command = commandClass.newInstance();
                response = command.execute(request, binaryData, context);
            }
        } catch (Throwable t) {
            MsgLogger.LOG.errorCommandExecutionFailureUIClient(requestClassName, session.getId(), t);
            String errorMessage = "Command failed [" + requestClassName + "]";
            response = new GenericErrorResponseBuilder()
                    .setThrowable(t)
                    .setErrorMessage(errorMessage)
                    .build();

        }

        String responseText = (response == null) ? null : ApiDeserializer.toHawkularFormat(response);
        return responseText;
    }

    @OnClose
    public void uiClientSessionClose(Session session, CloseReason reason) {
        MsgLogger.LOG.infoUISessionClosed(session.getId(), reason);
        connectedUIClients.removeSession(session);
    }
}
