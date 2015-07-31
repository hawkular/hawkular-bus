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

import java.io.InputStream;

/**
 * If extra binary data came over with a command, this object will contain methods to access that binary data.
 */
public class BinaryData {

    private final byte[] inMemoryData;
    private final InputStream streamData;

    public BinaryData(byte[] inMemoryData, InputStream streamData) {
        this.inMemoryData = (inMemoryData != null) ? inMemoryData : new byte[0];
        this.streamData = streamData;
    }

    /**
     * There might have been data already taken off of the {@link #getStreamData() stream} during
     * processing of the incoming command (typically due to the JSON parser eagerly slurping
     * data). If so, this will be that data.
     *
     * If the returned array is non-zero in length, its data should be considered coming first before
     * anything read from {@link #getStreamData() the stream}.
     *
     * @return data already in memory that has been taken out of the stream. Will not be null but may be empty.
     */
    public byte[] getInMemoryData() {
        return inMemoryData;
    }

    /**
     * This stream contains additional binary data that was sent with the command message.
     * Data may have already been taken out of this stream - if so, that data that is now in-memory
     * can be accessed via {@link #getInMemoryData()}. Note that the in-memory data should be considered
     * data that comes first ahead of any data taken from this stream.
     *
     * @return stream containing additional data from the remote client
     */
    public InputStream getStreamData() {
        return streamData;
    }
}
