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
package org.hawkular.feedcomm.ws.mdb;

import javax.websocket.Session;

import org.hawkular.bus.common.BasicMessageWithExtraData;
import org.hawkular.bus.common.consumer.BasicMessageListener;
import org.hawkular.feedcomm.api.ExecuteOperationResponse;
import org.hawkular.feedcomm.ws.Constants;
import org.hawkular.feedcomm.ws.MsgLogger;
import org.hawkular.feedcomm.ws.WebSocketHelper;
import org.hawkular.feedcomm.ws.server.ConnectedUIClients;

public class ExecuteOperationResponseListener extends BasicMessageListener<ExecuteOperationResponse> {

    private ConnectedUIClients connectedUIClients;

    public ExecuteOperationResponseListener(ConnectedUIClients connectedUIClients) {
        this.connectedUIClients = connectedUIClients;
    }

    protected void onBasicMessage(BasicMessageWithExtraData<ExecuteOperationResponse> responseWithData) {
        try {
            ExecuteOperationResponse response = responseWithData.getBasicMessage();
            String uiClientId = response.getHeaders().get(Constants.HEADER_UICLIENTID);
            if (uiClientId == null) {
                // TODO: for now, just send it to all UI clients on our server (we don't really want this behavior)
                //       we really want to those this exception since in the future the header must be there
                //throw new IllegalArgumentException("Missing header: " + Constants.HEADER_UICLIENTID);
                MsgLogger.LOG.warnf("HACK: Telling ALL UI that operation [%s] on resource [%s] resulted in [%s]",
                        response.getOperationName(), response.getResourcePath(), response.getStatus());
                new WebSocketHelper().sendBasicMessageAsync(connectedUIClients.getAllSessions(), response);
                return;
            }

            // we are assuming the UI client ID *is* the session ID
            Session session = connectedUIClients.getSessionBySessionId(uiClientId);
            if (session == null) {
                return; // we don't have the UI client, this message isn't for us
            }

            MsgLogger.LOG.infof("Telling UI client [%s] that operation [%s] on resource [%s] resulted in [%s]",
                    uiClientId, response.getOperationName(), response.getResourcePath(), response.getStatus());

            // send the request to the UI client
            new WebSocketHelper().sendBasicMessageAsync(session, response);
            return;

        } catch (Exception e) {
            // catch all exceptions and just log the error to let us auto-ack the message anyway
            MsgLogger.LOG.errorCannotProcessExecuteOperationResponseMessage(e);
        }
    }
}
