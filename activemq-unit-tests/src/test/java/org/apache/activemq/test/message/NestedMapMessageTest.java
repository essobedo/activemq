/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.test.message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;

import org.apache.activemq.test.JmsTopicSendReceiveWithTwoConnectionsAndEmbeddedBrokerTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class NestedMapMessageTest extends JmsTopicSendReceiveWithTwoConnectionsAndEmbeddedBrokerTest {

    private static final Logger LOG = LoggerFactory.getLogger(NestedMapMessageTest.class);

    @Override
    @SuppressWarnings("rawtypes")
    protected void assertMessageValid(int index, Message message) throws JMSException {
        assertTrue("Should be a MapMessage: " + message, message instanceof MapMessage);

        MapMessage mapMessage = (MapMessage)message;

        Object value = mapMessage.getObject("textField");
        assertEquals("textField", data[index], value);

        Map map = (Map)mapMessage.getObject("mapField");
        assertNotNull(map);
        assertEquals("mapField.a", "foo", map.get("a"));
        assertEquals("mapField.b", Integer.valueOf(23), map.get("b"));
        assertEquals("mapField.c", Long.valueOf(45), map.get("c"));

        value = map.get("d");
        assertTrue("mapField.d should be a Map", value instanceof Map);
        map = (Map)value;

        assertEquals("mapField.d.x", "abc", map.get("x"));
        value = map.get("y");
        assertTrue("mapField.d.y is a List", value instanceof List);
        List list = (List)value;
        LOG.debug("mapField.d.y: " + list);
        assertEquals("listField.size", 3, list.size());

        LOG.debug("Found map: " + map);

        list = (List)mapMessage.getObject("listField");
        LOG.debug("listField: " + list);
        assertEquals("listField.size", 3, list.size());
        assertEquals("listField[0]", "a", list.get(0));
        assertEquals("listField[1]", "b", list.get(1));
        assertEquals("listField[2]", "c", list.get(2));
    }

    @Override
    protected Message createMessage(int index) throws JMSException {
        MapMessage answer = session.createMapMessage();

        answer.setString("textField", data[index]);

        Map<String, Object> grandChildMap = new HashMap<String, Object>();
        grandChildMap.put("x", "abc");
        grandChildMap.put("y", Arrays.asList(new Object[] {"a", "b", "c"}));

        Map<String, Object> nestedMap = new HashMap<String, Object>();
        nestedMap.put("a", "foo");
        nestedMap.put("b", Integer.valueOf(23));
        nestedMap.put("c", Long.valueOf(45));
        nestedMap.put("d", grandChildMap);

        answer.setObject("mapField", nestedMap);
        answer.setObject("listField", Arrays.asList(new Object[] {"a", "b", "c"}));

        return answer;
    }

}
