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
import org.hawkular.feedcomm.api.DeployApplicationResponse;
import org.hawkular.feedcomm.ws.Constants;
import org.hawkular.feedcomm.ws.MsgLogger;
import org.hawkular.feedcomm.ws.WebSocketHelper;
import org.hawkular.feedcomm.ws.server.ConnectedUIClients;

public class DeployApplicationResponseListener extends BasicMessageListener<DeployApplicationResponse> {

    private ConnectedUIClients connectedUIClients;

    public DeployApplicationResponseListener(ConnectedUIClients connectedUIClients) {
        this.connectedUIClients = connectedUIClients;
    }

    protected void onBasicMessage(BasicMessageWithExtraData<DeployApplicationResponse> responseWithData) {
        try {
            DeployApplicationResponse response = responseWithData.getBasicMessage();
            String uiClientId = response.getHeaders().get(Constants.HEADER_UICLIENTID);
            if (uiClientId == null) {
                // TODO: for now, just send it to all UI clients on our server (we don't really want this behavior)
                //       we really want to those this exception since in the future the header must be there
                //throw new IllegalArgumentException("Missing header: " + Constants.HEADER_UICLIENTID);
                MsgLogger.LOG.warnf(
                        "HACK: Telling ALL UI that app [%s] deploy on resource ID [%s] resulted in [%s][%s]",
                        response.getDestinationFileName(), response.getResourcePath(), response.getStatus(),
                        response.getMessage());
                new WebSocketHelper().sendBasicMessageAsync(connectedUIClients.getAllSessions(), response);
                return;
            }

            // we are assuming the UI client ID *is* the session ID
            Session session = connectedUIClients.getSessionBySessionId(uiClientId);
            if (session == null) {
                return; // we don't have the UI client, this message isn't for us
            }

            MsgLogger.LOG.infof(
                    "Telling UI client [%s] that app [%s] deployment on resource ID [%s] resulted in [%s][%s]",
                    uiClientId, response.getDestinationFileName(), response.getResourcePath(), response.getStatus(),
                    response.getMessage());

            // send the request to the UI client
            new WebSocketHelper().sendBasicMessageAsync(session, response);
            return;

        } catch (Exception e) {
            // catch all exceptions and just log the error to let us auto-ack the message anyway
            MsgLogger.LOG.errorf(e, "Cannot process DeployApplicationResponse message");
        }
    }
}
