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

import java.io.IOException;
import java.io.InputStream;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.hawkular.accounts.websocket.Authenticator;
import org.hawkular.accounts.websocket.WebsocketAuthenticationException;
import org.hawkular.bus.common.BasicMessage;
import org.hawkular.bus.common.BasicMessageWithExtraData;
import org.hawkular.bus.common.BinaryData;
import org.hawkular.feedcomm.api.ApiDeserializer;
import org.hawkular.feedcomm.api.AuthMessage;
import org.hawkular.feedcomm.api.Authentication;
import org.hawkular.feedcomm.api.GenericErrorResponseBuilder;
import org.hawkular.feedcomm.ws.Constants;
import org.hawkular.feedcomm.ws.MsgLogger;
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

    @Inject
    private UIClientListenerGenerator uiClientListenerGenerator;

    @Inject
    private Authenticator authenticator;

    @OnOpen
    public void uiClientSessionOpen(Session session) {
        MsgLogger.LOG.infoUIClientSessionOpened(session.getId());
        connectedUIClients.addSession(session);

        // hook up the UI client to the message bus to receive messages from feeds that are on other servers
        String uiClientID = getUIClientIDFromSession(session);
        try {
            uiClientListenerGenerator.addListeners(uiClientID);
        } catch (Exception e) {
            MsgLogger.LOG.errorFailedToAddMessageListenersForUIClient(uiClientID, session.getId(), e);
            try {
                session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, "Internal server error"));
            } catch (IOException ioe) {
                MsgLogger.LOG.errorf(ioe,
                        "Failed to close UI client [%s] (session [%s]) after internal server error",
                        uiClientID, session.getId());
            }
        }
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
        String uiClientId = getUIClientIDFromSession(session);
        MsgLogger.LOG.infoReceivedMessageFromUIClient(uiClientId, session.getId());

        String requestClassName = "?";
        BasicMessage response;

        try {
            // parse the JSON and get its message POJO
            BasicMessage request = new ApiDeserializer().deserialize(nameAndJsonStr);
            requestClassName = request.getClass().getName();

            // make sure the user is authenticated
            authenticate(request, session);

            // determine what command this JSON request needs to execute, and execute it
            Class<? extends Command<?, ?>> commandClass = Constants.VALID_COMMANDS_FROM_UI.get(requestClassName);
            if (commandClass == null) {
                MsgLogger.LOG.errorInvalidCommandRequestUIClient(uiClientId, session.getId(), requestClassName);
                String errorMessage = "Invalid command request: " + requestClassName;
                response = new GenericErrorResponseBuilder().setErrorMessage(errorMessage).build();
            } else {
                CommandContext context = new CommandContext(connectedFeeds, connectedUIClients,
                        uiClientListenerGenerator.getConnectionFactory(), session);
                Command command = commandClass.newInstance();
                response = command.execute(request, null, context);
            }
        } catch (WebsocketAuthenticationException wae) {
            response = null;
            try {
                session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, wae.getLocalizedMessage()));
            } catch (IOException ioe) {
                MsgLogger.LOG.errorf(ioe,
                        "Failed to close UI client [%s] (session [%s]) after authentication failure (request=[%s])",
                        uiClientId, session.getId(), requestClassName);
            }
        } catch (Throwable t) {
            MsgLogger.LOG.errorCommandExecutionFailureUIClient(requestClassName, uiClientId, session.getId(), t);
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
        String uiClientId = getUIClientIDFromSession(session);
        MsgLogger.LOG.infoReceivedBinaryDataFromUIClient(session.getId());

        String requestClassName = "?";
        BasicMessage response;

        try {
            // parse the JSON and get its message POJO, including any additional binary data being streamed
            BasicMessageWithExtraData<BasicMessage> reqWithData = new ApiDeserializer().deserialize(binaryDataStream);
            BasicMessage request = reqWithData.getBasicMessage();
            BinaryData binaryData = reqWithData.getBinaryData();
            requestClassName = request.getClass().getName();

            // make sure the user is authenticated
            authenticate(request, session);

            // determine what command this JSON request needs to execute, and execute it
            Class<? extends Command<?, ?>> commandClass = Constants.VALID_COMMANDS_FROM_UI.get(requestClassName);
            if (commandClass == null) {
                MsgLogger.LOG.errorInvalidCommandRequestUIClient(uiClientId, session.getId(), requestClassName);
                String errorMessage = "Invalid command request: " + requestClassName;
                response = new GenericErrorResponseBuilder().setErrorMessage(errorMessage).build();
            } else {
                CommandContext context = new CommandContext(connectedFeeds, connectedUIClients,
                        uiClientListenerGenerator.getConnectionFactory(), session);
                Command command = commandClass.newInstance();
                response = command.execute(request, binaryData, context);
            }
        } catch (WebsocketAuthenticationException wae) {
            response = null;
            try {
                session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, wae.getLocalizedMessage()));
            } catch (IOException ioe) {
                MsgLogger.LOG.errorf(ioe,
                        "Failed to close UI client [%s] (session [%s]) after authentication failure (request=[%s])",
                        uiClientId, session.getId(), requestClassName);
            }
        } catch (Throwable t) {
            MsgLogger.LOG.errorCommandExecutionFailureUIClient(requestClassName, uiClientId, session.getId(), t);
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
        String uiClientId = getUIClientIDFromSession(session);
        MsgLogger.LOG.infoUISessionClosed(uiClientId, session.getId(), reason);
        connectedUIClients.removeSession(session);
        uiClientListenerGenerator.removeListeners(uiClientId);
    }

    private String getUIClientIDFromSession(Session session) {
        return session.getId(); // for now, the UI client ID *is* the session ID
        //return session.getRequestParameterMap().get("Hawkular-UI-Client-ID").get(0);
    }

    private void authenticate(BasicMessage basicMessage, Session session) throws WebsocketAuthenticationException {
        // if no authentication information is passed in the message, we will still ask Hawkular Accounts
        // to authenticate our session. This is to ensure any previous credentials/token that was authenticated
        // in the past is still valid now.

        String username = null;
        String password = null;
        String token = null;
        String persona = null;

        if (basicMessage instanceof AuthMessage) {
            AuthMessage authMessage = (AuthMessage) basicMessage;
            Authentication auth = authMessage.getAuthentication();
            if (auth != null) {
                username = auth.getUsername();
                password = auth.getPassword();
                token = auth.getToken();
                persona = auth.getPersona();
            }
        }

        try {
            // note that if both username and token are provided, we authenticate using the token
            boolean hasToken = (token != null && !token.isEmpty());
            if (hasToken) {
                MsgLogger.LOG.tracef("authenticating token [%s/%s], session=[%s]", token, persona, session.getId());
                authenticator.authenticateWithToken(token, persona, session);
            } else {
                MsgLogger.LOG.tracef("authenticating user [%s/%s/%s], session=[%s]", username, password, persona,
                        session.getId());
                authenticator.authenticateWithCredentials(username, password, persona, session);
            }
        } catch (WebsocketAuthenticationException wae) {
            throw wae;
        } catch (Exception e) {
            throw new WebsocketAuthenticationException("Unauthorized!", e);
        }

        return; // authentication successful
    }
}
