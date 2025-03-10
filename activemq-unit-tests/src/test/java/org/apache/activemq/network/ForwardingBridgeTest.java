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
package org.apache.activemq.network;

import jakarta.jms.DeliveryMode;

import junit.framework.Test;

import org.apache.activemq.broker.StubConnection;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.SessionInfo;

public class ForwardingBridgeTest extends NetworkTestSupport {

    public ActiveMQDestination destination;
    public byte destinationType;
    public int deliveryMode;
    private ForwardingBridge bridge;

    public void initCombosForTestForwardMessageCompressed() {
        addCombinationValues("deliveryMode", new Object[] {Integer.valueOf(DeliveryMode.NON_PERSISTENT),
                                                           Integer.valueOf(DeliveryMode.PERSISTENT)});
        addCombinationValues("destinationType", new Object[] {Byte.valueOf(ActiveMQDestination.QUEUE_TYPE),
                                                              Byte.valueOf(ActiveMQDestination.TOPIC_TYPE)});
    }

    public void testForwardMessageCompressed() throws Exception {

        bridge.setUseCompression(true);

        // Start a producer on local broker
        StubConnection connection1 = createConnection();
        ConnectionInfo connectionInfo1 = createConnectionInfo();
        SessionInfo sessionInfo1 = createSessionInfo(connectionInfo1);
        ProducerInfo producerInfo = createProducerInfo(sessionInfo1);
        connection1.send(connectionInfo1);
        connection1.send(sessionInfo1);
        connection1.send(producerInfo);

        destination = createDestinationInfo(connection1, connectionInfo1, destinationType);

        // Start a consumer on a remote broker
        StubConnection connection2 = createRemoteConnection();
        ConnectionInfo connectionInfo2 = createConnectionInfo();
        SessionInfo sessionInfo2 = createSessionInfo(connectionInfo2);
        connection2.send(connectionInfo2);
        connection2.send(sessionInfo2);
        ConsumerInfo consumerInfo = createConsumerInfo(sessionInfo2, destination);
        connection2.send(consumerInfo);
        Thread.sleep(1000);
        // Give forwarding bridge a chance to finish setting up
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        // Send the message to the local boker.
        connection1.send(createMessage(producerInfo, destination, deliveryMode));

        // Make sure the message was delivered via the remote.
        Message m = receiveMessage(connection2);
        assertNotNull(m);

        // Make sure its compressed now
        ActiveMQMessage message = (ActiveMQMessage) m;
        assertTrue(message.isCompressed());
    }

    public void initCombosForTestAddConsumerThenSend() {
        addCombinationValues("deliveryMode", new Object[] {Integer.valueOf(DeliveryMode.NON_PERSISTENT),
                                                           Integer.valueOf(DeliveryMode.PERSISTENT)});
        addCombinationValues("destinationType", new Object[] {Byte.valueOf(ActiveMQDestination.QUEUE_TYPE),
                                                              Byte.valueOf(ActiveMQDestination.TOPIC_TYPE)});
    }

    public void testAddConsumerThenSend() throws Exception {
        // Start a producer on local broker
        StubConnection connection1 = createConnection();
        ConnectionInfo connectionInfo1 = createConnectionInfo();
        SessionInfo sessionInfo1 = createSessionInfo(connectionInfo1);
        ProducerInfo producerInfo = createProducerInfo(sessionInfo1);
        connection1.send(connectionInfo1);
        connection1.send(sessionInfo1);
        connection1.send(producerInfo);

        destination = createDestinationInfo(connection1, connectionInfo1, destinationType);

        // Start a consumer on a remote broker
        StubConnection connection2 = createRemoteConnection();
        ConnectionInfo connectionInfo2 = createConnectionInfo();
        SessionInfo sessionInfo2 = createSessionInfo(connectionInfo2);
        connection2.send(connectionInfo2);
        connection2.send(sessionInfo2);
        ConsumerInfo consumerInfo = createConsumerInfo(sessionInfo2, destination);
        connection2.send(consumerInfo);
        Thread.sleep(1000);
        // Give forwarding bridge a chance to finish setting up
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        // Send the message to the local boker.
        connection1.send(createMessage(producerInfo, destination, deliveryMode));

        // Make sure the message was delivered via the remote.

        Message m = receiveMessage(connection2);
        assertNotNull(m);
    }

    protected void setUp() throws Exception {
        super.setUp();
        bridge = new ForwardingBridge(createTransport(), createRemoteTransport());
        bridge.setClientId("local-remote-bridge");
        bridge.setDispatchAsync(false);
        bridge.start();
    }

    protected void tearDown() throws Exception {
        bridge.stop();
        super.tearDown();
    }

    public static Test suite() {
        return suite(ForwardingBridgeTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
