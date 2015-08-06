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
package org.hawkular.feedcomm.ws.command;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.bus.common.BinaryData;
import org.hawkular.feedcomm.api.GenericErrorResponse;
import org.hawkular.feedcomm.ws.MsgLogger;

/**
 * This processes any generic error response that is sent to the server. This will usually
 * happen when the server sent a message to a remote feed or UI but that remote endpoint
 * failed to process the request.
 */
public class GenericErrorResponseCommand implements Command<GenericErrorResponse, BasicMessage> {
    public static final Class<GenericErrorResponse> REQUEST_CLASS = GenericErrorResponse.class;

    @Override
    public BasicMessage execute(GenericErrorResponse errorResponse, BinaryData binaryData, CommandContext context)
            throws Exception {

        String errorMessage = errorResponse.getErrorMessage();
        String stackTrace = errorResponse.getStackTrace();

        MsgLogger.LOG.warnReceivedGenericErrorResponse(errorMessage, stackTrace);

        return null; // nothing to send back
    }
}
