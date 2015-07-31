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
package org.hawkular.feedcomm.ws.command.feed;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.feedcomm.api.ExecuteOperationResponse;
import org.hawkular.feedcomm.ws.MsgLogger;
import org.hawkular.feedcomm.ws.WebSocketHelper;
import org.hawkular.feedcomm.ws.command.BinaryData;
import org.hawkular.feedcomm.ws.command.Command;
import org.hawkular.feedcomm.ws.command.CommandContext;

/**
 * A feed telling us it finished its operation execution attempt.
 */
public class ExecuteOperationResponseCommand implements Command<ExecuteOperationResponse, BasicMessage> {
    public static final Class<ExecuteOperationResponse> REQUEST_CLASS = ExecuteOperationResponse.class;

    @Override
    public BasicMessage execute(ExecuteOperationResponse response, BinaryData binaryData, CommandContext context)
            throws Exception {

        String resId = response.getResourceId();
        String opName = response.getOperationName();
        String status = response.getStatus();
        String msg = response.getMessage();
        MsgLogger.LOG.infof("Operation execution completed. Resource=[%s], Operation=[%s], Status=[%s], Message=[%s]",
                resId, opName, status, msg);

        // TODO: today we don't know which UI sent the original request, so for now, tell all UIs what the response was
        new WebSocketHelper().sendBasicMessageSync(context.getConnectedUIClients().getAllSessions(), response);

        return null;
    }
}
