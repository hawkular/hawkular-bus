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

import org.junit.Test;

public class ObjectMessageTest {
    @Test
    public void testObjectMessageNoArgConstructor() {
        ObjectMessage msg = new ObjectMessage();
        try {
            msg.getObject();
            assert false : "should have failed - didn't tell it the class";
        } catch (IllegalStateException expected) {
        }

        msg.setMessage("foo");
        msg.setObjectClass(String.class);
        Object o = msg.getObject();
        assert o.getClass() == String.class;
        assert o.toString().equals("foo");
    }

    @Test
    public void testObjectMessage() {
        ObjectMessage msg = new ObjectMessage(new String("bar"));
        Object o = msg.getObject();
        assert o.getClass() == String.class;
        assert o.toString().equals("bar");
    }

    @Test
    public void testCustomObjectMessage() {
        MyObj myobj = new MyObj();
        myobj.letters = "abc";
        myobj.number = 123;

        // pass object to constructor
        ObjectMessage msg = new ObjectMessage(myobj);
        Object o = msg.getObject();
        assert o.getClass() == MyObj.class;
        assert ((MyObj) o).letters.equals("abc");
        assert ((MyObj) o).number == 123;

        assert msg.toJSON().equals("{\"letters\":\"abc\",\"number\":123}") : msg.toJSON();
        assert msg.toJSON().equals(msg.getMessage()) : msg.getMessage();

        // pass class to constructor
        msg = new ObjectMessage(MyObj.class);
        msg.setMessage("{\"letters\":\"xyz\",\"number\":987}");
        assert msg.toJSON().equals("{\"letters\":\"xyz\",\"number\":987}") : msg.toJSON();
        assert ((MyObj) msg.getObject()).letters.equals("xyz");
        assert ((MyObj) msg.getObject()).number == 987;

        // pass nothing to constructor
        msg = new ObjectMessage();
        msg.setObjectClass(MyObj.class);
        msg.setMessage("{\"letters\":\"xyz\",\"number\":987}");
        assert msg.toJSON().equals("{\"letters\":\"xyz\",\"number\":987}") : msg.toJSON();
        assert ((MyObj) msg.getObject()).letters.equals("xyz");
        assert ((MyObj) msg.getObject()).number == 987;
    }
}

class MyObj {
    public String letters;
    public int number;
}
