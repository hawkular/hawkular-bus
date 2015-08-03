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
package org.hawkular.bus.common.consumer;

import javax.jms.Message;

import org.hawkular.bus.common.BasicMessage;
import org.hawkular.bus.common.BasicMessageWithExtraData;

/**
 * A message listener that expects to receive a JSON-encoded BasicMessage or one of its subclasses. Implementors need
 * only implement the method that takes an BasicRecord or one of its subclasses; the JSON decoding is handled for you.
 *
 * This processes fire-and-forget requests - that is, the request message is processed with no response being sent back
 * to the sender.
 *
 * @author John Mazzitelli
 */

public abstract class BasicMessageListener<T extends BasicMessage> extends AbstractBasicMessageListener<T> {

    public BasicMessageListener() {
        super();
    }

    protected BasicMessageListener(Class<T> jsonDecoderRing) {
        super(jsonDecoderRing);
    }

    @Override
    public void onMessage(Message message) {
        BasicMessageWithExtraData<T> msgWithExtraData = parseMessage(message);
        if (msgWithExtraData == null) {
            return; // either we are not to process this message or some error occurred, so we skip it
        }

        onBasicMessage(msgWithExtraData);
        return;
    };

    /**
     * Subclasses implement this method to process the received message.
     *
     * @param msgWithExtraData the basic message received with any extra data that came with it
     */
    protected abstract void onBasicMessage(BasicMessageWithExtraData<T> msgWithExtraData);
}
