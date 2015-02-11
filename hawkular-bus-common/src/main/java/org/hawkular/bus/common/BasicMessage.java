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
package org.hawkular.bus.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Basic information that is sent over the message bus.
 *
 * The {@link #getMessageId() message ID} is assigned by the messaging framework and so typically is not explicitly set.
 *
 * The {@link #getCorrelationId() correlation ID} is a message ID of another message that was sent previously. This is
 * usually left unset unless this message needs to be correlated with another. As an example, when a process is stopped,
 * you can correlate the "Stopped" event with the "Stopping" event so you can later determine how long it took for the
 * process to stop.
 *
 * The {@link #getHeaders() headers} are normally those out-of-band properties that are sent with the message.
 */
public abstract class BasicMessage {
    // these are passed out-of-band of the message body - these attributes will therefore not be JSON encoded
    private MessageId _messageId;
    private MessageId _correlationId;
    private Map<String, String> _headers;

    /**
     * Convenience static method that converts a JSON string to a particular message object.
     *
     * @param json the JSON string
     * @param clazz the class whose instance is represented by the JSON string
     *
     * @return the message object that was represented by the JSON string
     */
    public static <T extends BasicMessage> T fromJSON(String json, Class<T> clazz) {
        final Gson gson = createGsonBuilder();
        return gson.fromJson(json, clazz);
    }

    /**
     * Converts this message to its JSON string representation.
     *
     * @return JSON encoded data that represents this message.
     */
    public String toJSON() {
        final Gson gson = createGsonBuilder();
        return gson.toJson(this);
    }

    protected BasicMessage() {
        // Intentionally left blank
    }

    /**
     * Returns the message ID that was assigned to this message by the messaging infrastructure. This could be null if
     * the message has not been sent yet.
     *
     * @return message ID assigned to this message by the messaging framework
     */
    public MessageId getMessageId() {
        return _messageId;
    }

    public void setMessageId(MessageId messageId) {
        this._messageId = messageId;
    }

    /**
     * If this message is correlated with another message, this will be that other message's ID. This could be null if
     * the message is not correlated with another message.
     *
     * @return the message ID of the correlated message
     */
    public MessageId getCorrelationId() {
        return _correlationId;
    }

    public void setCorrelationId(MessageId correlationId) {
        this._correlationId = correlationId;
    }

    /**
     * The headers that were shipped along side of the message when the message was received.
     * The returned map is an unmodifiable read-only view of the properties.
     * Will never return <code>null</code> but may return an empty map.
     *
     * @return a read-only view of the name/value properties that came with the message as separate headers.
     */
    public Map<String, String> getHeaders() {
        if (_headers == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(_headers);
    }

    /**
     * Sets headers that will be sent with the message when the message gets delivered.
     * This completely replaces any existing headers already associated with this message.
     * Note that the given name/value pairs will be copied to an internal map.
     * If the given map is null or empty, this message's internal map will be destroyed
     * and {@link #getHeaders()} will return an empty map.
     *
     * Note that the header values are all expected to be strings.
     *
     * @param headers name/value properties
     */
    public void setHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            this._headers = null;
        } else {
            if (this._headers == null) {
                this._headers = new HashMap<String, String>(headers);
            } else {
                // we want to replace what we had with the new headers
                this._headers.clear();
                this._headers.putAll(headers);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.getClass().getSimpleName() + ": [");
        str.append("message-id=");
        str.append(getMessageId());
        str.append(", correlation-id=");
        str.append(getCorrelationId());
        str.append(", headers=");
        str.append(getHeaders());
        str.append(", json-body=[");
        str.append(toJSON());
        str.append("]]");
        return str.toString();
    }

    protected static Gson createGsonBuilder() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }
}
