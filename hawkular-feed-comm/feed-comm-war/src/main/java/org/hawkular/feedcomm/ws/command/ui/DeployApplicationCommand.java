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
package org.hawkular.feedcomm.ws.command.ui;

import java.util.Collections;
import java.util.Map;

import org.hawkular.bus.common.BinaryData;
import org.hawkular.bus.common.ConnectionContextFactory;
import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.MessageProcessor;
import org.hawkular.bus.common.producer.ProducerConnectionContext;
import org.hawkular.feedcomm.api.DeployApplicationRequest;
import org.hawkular.feedcomm.api.GenericSuccessResponse;
import org.hawkular.feedcomm.ws.Constants;
import org.hawkular.feedcomm.ws.MsgLogger;
import org.hawkular.feedcomm.ws.command.Command;
import org.hawkular.feedcomm.ws.command.CommandContext;
import org.hawkular.inventory.api.model.CanonicalPath;

/**
 * UI client requesting to send a file to a remote feed.
 */
public class DeployApplicationCommand implements Command<DeployApplicationRequest, GenericSuccessResponse> {
    public static final Class<DeployApplicationRequest> REQUEST_CLASS = DeployApplicationRequest.class;

    @Override
    public GenericSuccessResponse execute(DeployApplicationRequest request, BinaryData binaryData,
            CommandContext context) throws Exception {

        // determine what feed needs to be sent the message
        CanonicalPath resourcePath = CanonicalPath.fromString(request.getResourcePath());
        String feedId = resourcePath.ids().getFeedId();

        try (ConnectionContextFactory ccf = new ConnectionContextFactory(context.getConnectionFactory())) {
            Endpoint endpoint = Constants.DEST_FEED_DEPLOY_APPLICATION;
            ProducerConnectionContext pcc = ccf.createProducerConnectionContext(endpoint);
            Map<String, String> feedIdHeader = Collections.singletonMap(Constants.HEADER_FEEDID, feedId);
            MessageId mid = new MessageProcessor().sendWithBinaryData(pcc, request, binaryData, feedIdHeader);
            MsgLogger.LOG.debugf("File upload request placed on bus. mid=[%s], request=[%s]", mid, request);
            GenericSuccessResponse response = new GenericSuccessResponse();
            response.setMessage("The execution request has been forwarded to feed [" + feedId + "] (id=" + mid + ")");
            return response;
        }
    }
}
