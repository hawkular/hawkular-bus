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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // prohibit instantiation
    private JsonUtil() {
    }

    /**
     * This will parse a JSON object whose JSON text is in the given input stream.
     * The input stream will remain open so the caller can stream any extra data that might
     * appear after it.
     *
     * Because of the way the JSON parser works, some extra data might have been read from the stream
     * that wasn't part of the JSON message. In that case, a non-empty byte array containing the extra read
     * data is returned in the map value.
     *
     * @param in input stream that has a JSON message at the head.
     * @param clazz the Java class representing the type of JSON object
     * @return a map whose key is the JSON object that was parsed and whose value is a byte array containing
     *         extra data that was read from the stream but not part of the JSON message.
     */
    public static <T> Map<T, byte[]> fromJson(InputStream in, Class<T> clazz) {
        final T obj;
        final byte[] remainder;
        try (JsonParser parser = new JsonFactory().configure(Feature.AUTO_CLOSE_SOURCE, false).createParser(in)) {
            obj = OBJECT_MAPPER.readValues(parser, clazz).next();
            ByteArrayOutputStream remainderStream = new ByteArrayOutputStream();
            int released = parser.releaseBuffered(remainderStream);
            remainder = (released > 0) ? remainderStream.toByteArray() : new byte[0];
        } catch (Exception e) {
            throw new IllegalArgumentException("Stream cannot be converted to JSON object of type [" + clazz + "]", e);
        }
        return Collections.singletonMap(obj, remainder);
    }
}
