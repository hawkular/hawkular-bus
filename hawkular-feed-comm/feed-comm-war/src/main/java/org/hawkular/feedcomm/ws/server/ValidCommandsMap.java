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

import java.util.HashMap;
import java.util.Map;

import org.hawkular.feedcomm.ws.command.Command;

/**
 * Contains commands that are valid for a particular endpoint to execute.
 * This is here mainly to have a "put" method that returns this object so a valid commands map
 * can be created by chaining "put" calls.
 */
public class ValidCommandsMap {
    private Map<String, Class<? extends Command<?, ?>>> validCommands = new HashMap<>();

    public ValidCommandsMap() {
    }

    public ValidCommandsMap put(String commandName, Class<? extends Command<?, ?>> command) {
        validCommands.put(commandName, command);
        return this;
    }

    public Class<? extends Command<?, ?>> get(String commandName) {
        return validCommands.get(commandName);
    }
}
