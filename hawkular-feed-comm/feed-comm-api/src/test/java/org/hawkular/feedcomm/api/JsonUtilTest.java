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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.hawkular.bus.common.BasicMessage;
import org.junit.Assert;
import org.junit.Test;

public class JsonUtilTest {

    @Test
    public void testReadFromInputStream() throws IOException {
        // tests that this can extract the JSON even if more data follows in the stream
        JsonUtilTestMessage msg = new JsonUtilTestMessage();
        msg.testString = "test-string";
        msg.testInt = 1;
        msg.testDouble = 9.9;

        String json = msg.toJSON();
        String extra = "This is extra";
        String jsonPlusExtra = json + extra;

        ByteArrayInputStream in = new UncloseableByteArrayInputStream(jsonPlusExtra.getBytes());

        Map<JsonUtilTestMessage, byte[]> fromJsonMap = JsonUtil.fromJson(in, JsonUtilTestMessage.class);
        JsonUtilTestMessage msg2 = fromJsonMap.keySet().iterator().next();
        byte[] leftoverFromJsonParser = fromJsonMap.values().iterator().next();

        Assert.assertEquals(msg.testString, msg2.testString);
        Assert.assertEquals(msg.testInt, msg2.testInt);
        Assert.assertEquals(msg.testDouble, msg2.testDouble, 0.1);

        // now make sure the stream still has our extra data that we can read now
        byte[] leftoverFromStream = new byte[in.available()];
        in.read(leftoverFromStream);

        String totalRemaining = new String(leftoverFromJsonParser, "UTF-8") + new String(leftoverFromStream, "UTF-8");
        Assert.assertEquals(extra.length(), totalRemaining.length());
        Assert.assertEquals(extra, totalRemaining);
    }
}

// This is just to test that our JsonUtil tells the JsonParser to NOT close the stream.
// If close is called, that is bad and should fail the test
class UncloseableByteArrayInputStream extends ByteArrayInputStream {
    public UncloseableByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    @Override
    public void close() throws IOException {
        Assert.fail("The input stream should NOT have been closed");
    }
}

class JsonUtilTestMessage extends BasicMessage {
    public String testString;
    public int testInt;
    public double testDouble;

    public JsonUtilTestMessage() {
    }
}
