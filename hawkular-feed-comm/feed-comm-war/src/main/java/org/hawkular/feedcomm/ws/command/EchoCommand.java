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

import java.util.NoSuchElementException;
import java.util.Scanner;

import org.hawkular.bus.common.BinaryData;
import org.hawkular.feedcomm.api.EchoRequest;
import org.hawkular.feedcomm.api.EchoResponse;

public class EchoCommand implements Command<EchoRequest, EchoResponse> {
    public static final Class<EchoRequest> REQUEST_CLASS = EchoRequest.class;

    @Override
    public EchoResponse execute(EchoRequest echoRequest, BinaryData binaryData, CommandContext context) {
        String echo = String.format("ECHO [%s]", echoRequest.getEchoMessage());
        StringBuilder extra = new StringBuilder();

        if (binaryData != null) {
            try (Scanner scanner = new Scanner(binaryData, "UTF-8")) {
                scanner.useDelimiter("\\A");
                extra.append(scanner.next());
            } catch (NoSuchElementException nsee) {
            }
        }

        // return the response
        EchoResponse echoResponse = new EchoResponse();
        echoResponse.setReply(echo + extra.toString());
        return echoResponse;
    }
}
