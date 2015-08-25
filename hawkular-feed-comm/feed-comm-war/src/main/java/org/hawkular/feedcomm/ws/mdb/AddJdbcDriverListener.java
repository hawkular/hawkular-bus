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

import java.util.concurrent.ExecutorService;

import javax.websocket.Session;

import org.hawkular.bus.common.BasicMessageWithExtraData;
import org.hawkular.bus.common.BinaryData;
import org.hawkular.bus.common.consumer.BasicMessageListener;
import org.hawkular.feedcomm.api.AddJdbcDriverRequest;
import org.hawkular.feedcomm.api.ApiDeserializer;
import org.hawkular.feedcomm.ws.Constants;
import org.hawkular.feedcomm.ws.MsgLogger;
import org.hawkular.feedcomm.ws.WebSocketHelper;
import org.hawkular.feedcomm.ws.server.ConnectedFeeds;

public class AddJdbcDriverListener extends BasicMessageListener<AddJdbcDriverRequest> {

    private final ConnectedFeeds connectedFeeds;
    private final ExecutorService threadPool;

    public AddJdbcDriverListener(ConnectedFeeds connectedFeeds, ExecutorService threadPool) {
        this.connectedFeeds = connectedFeeds;
        this.threadPool = threadPool;
    }

    protected void onBasicMessage(BasicMessageWithExtraData<AddJdbcDriverRequest> request) {
        try {
            AddJdbcDriverRequest basicMessage = request.getBasicMessage();
            String feedId = basicMessage.getHeaders().get(Constants.HEADER_FEEDID);
            if (feedId == null) {
                throw new IllegalArgumentException("Missing header: " + Constants.HEADER_FEEDID);
            }
            Session session = connectedFeeds.getSession(feedId);
            if (session == null) {
                return; // we don't have the feed, this message isn't for us
            }

            MsgLogger.LOG.infof("Sending feed [%s] an JDBC Driver add on resource [%s]", feedId,
                    basicMessage.getResourcePath());

            // send the request to the feed
            BinaryData dataToSend = ApiDeserializer.toHawkularFormat(basicMessage, request.getBinaryData());
            new WebSocketHelper().sendBinaryAsync(session, dataToSend, threadPool);
            return;

        } catch (Exception e) {
            // catch all exceptions and just log the error to let us auto-ack the message anyway
            MsgLogger.LOG.errorf(e, "Cannot process deploy application request");
        }
    }
}
