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
package org.hawkular.feedcomm.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.hawkular.bus.common.BasicMessage;

/**
 * Given the special syntax of "apiName=JSON" this will deserialize the JSON into the appropriate API POJO.
 */
public class ApiDeserializer {

    // note that this assumes this class is in the same package as all the API POJOs
    private static final String API_PKG = ApiDeserializer.class.getPackage().getName();

    /**
     * Returns a string that encodes the given object as a JSON message but then
     * prefixes that JSON with additional information that a Hawkular client will
     * need to be able to deserialize the JSON.
     *
     * This string can be used to deserialize the object via {@link #deserialize(String)}.
     *
     * @param msg the message object that will be serialized into JSON
     * @return a string that includes the JSON that can be used by other Hawkular endpoints to deserialize the message.
     */
    public static String toHawkularFormat(BasicMessage msg) {
        return String.format("%s=%s", msg.getClass().getSimpleName(), msg.toJSON());
    }

    private static String[] fromHawkularFormat(String msg) {
        String[] nameAndJsonArray = msg.split("=", 2);
        if (nameAndJsonArray.length != 2) {
            throw new IllegalArgumentException("Cannot deserialize: [" + msg + "]");
        }
        return nameAndJsonArray;
    }

    public ApiDeserializer() {
    }

    /**
     * Deserializes a JSON string in {@link #toHawkularFormat(BasicMessage) Hawkular format}.
     * The JSON object is returned.
     *
     * @param nameAndJson the string to be deserialized
     * @return the object represented by the JSON
     */
    public <T extends BasicMessage> T deserialize(String nameAndJson) {
        String[] nameAndJsonArray = fromHawkularFormat(nameAndJson);
        String name = nameAndJsonArray[0];
        String json = nameAndJsonArray[1];

        // The name is the actual name of the POJO that is used to deserialize the JSON.
        // If not fully qualified with a package then assume it is in our package.
        if (name.indexOf(".") == -1) {
            name = String.format("%s.%s", API_PKG, name);
        }

        try {
            Class<T> pojo = (Class<T>) Class.forName(name);
            return BasicMessage.fromJSON(json, pojo);
        } catch (Exception e) {
            throw new RuntimeException("Cannot deserialize: [" + nameAndJson + "]", e);
        }
    }

    /**
     * Reads a JSON string in {@link #toHawkularFormat(BasicMessage) Hawkular format} that
     * is found in the given input stream and converts the JSON string to a particular message object.
     * The input stream will remain open so the caller can stream any extra data that might appear after it.
     *
     * Because of the way the JSON parser works, some extra data might have been read from the stream
     * that wasn't part of the JSON message. In that case, a non-empty byte array containing the extra read
     * data is returned in the map value.
     *
     * @param in input stream that has the Hawkular formatted JSON string at the head.
     *
     * @return a single-entry map whose key is the message object that was represented by the JSON string found
     *         in the stream. The value of the map is a byte array containing extra data that was read from
     *         the stream but not part of the JSON message.
     */
    public <T extends BasicMessage> Map<T, byte[]> deserialize(InputStream input) {
        // We know the format is "name=json" with possible extra data after it.
        // So first find the "name"
        StringBuilder nameBuilder = new StringBuilder();
        boolean foundSeparator = false;
        while (!foundSeparator) {
            int currentChar;
            try {
                currentChar = input.read();
            } catch (IOException ioe) {
                throw new RuntimeException("Cannot deserialize stream due to read error", ioe);
            }
            if (currentChar == -1) {
                throw new RuntimeException("Cannot deserialize stream - doesn't look valid");
            } else if (currentChar == '=') {
                foundSeparator = true;
            } else {
                nameBuilder.append((char) currentChar);
            }
        }

        // The name is the actual name of the POJO that is used to deserialize the JSON.
        // If not fully qualified with a package then assume it is in our package.
        String name = nameBuilder.toString();
        if (name.indexOf(".") == -1) {
            name = String.format("%s.%s", API_PKG, name);
        }

        // We now have the name and the input stream is pointing at the JSON
        try {
            Class<T> pojo = (Class<T>) Class.forName(name);
            return BasicMessage.fromJSON(input, pojo);
        } catch (Exception e) {
            throw new RuntimeException("Cannot deserialize stream with object [" + name + "]", e);
        }
    }
}
