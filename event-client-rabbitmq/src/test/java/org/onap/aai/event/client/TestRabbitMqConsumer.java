/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2018 European Software Marketing Ltd.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.event.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aai.event.api.MessageWithOffset;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;

@RunWith(MockitoJUnitRunner.class)
public class TestRabbitMqConsumer {
    @Mock
    public ConnectionFactory mockConnectionFactory;

    @Mock
    public Connection mockConnection;

    @Mock
    public Channel mockChannel;

    @Mock
    public DeclareOk mockDeclareOK;

    @Mock
    BlockingQueue<MessageWithOffset> mqueue;

    BuiltinExchangeType topicEx = BuiltinExchangeType.TOPIC;
    TimeUnit unit = TimeUnit.MILLISECONDS;
    Map<String, Integer> hostPortMap = new HashMap<String, Integer>();
    
    RabbitMqClientConfig config1 = new RabbitMqClientConfig();
    RabbitMqClientConfig config2 = new RabbitMqClientConfig();
    RabbitMqClientConfig config3 = new RabbitMqClientConfig();
    

    @Before
    public void init() throws Exception {
    	RabbitMqUtils.setConnectionFactory(mockConnectionFactory);
    	
    	config1.setHosts("host1:1234");
    	config1.setUsername("user");
    	config1.setPassword("secret");
    	config1.setExchangeName("my-exchange");
    	config1.setQueue("my-queue");
    	
    	config2.setHosts("host1:1234");
        config2.setUsername("user");
        config2.setPassword("secret");
        config2.setExchangeName("my-exchange");
        config2.setExchangeType(BuiltinExchangeType.DIRECT.name());
        config2.setQueue("my-queue");
        
        config3.setHosts("host1:1234,host2:5678");
        config3.setUsername("user");
        config3.setPassword("secret");
        config3.setQueue("my-queue");
    }

    @Test
    public void testConstructor() throws Exception {
       	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
        Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	Mockito.when(mockChannel.basicConsume(Mockito.any(), Mockito.anyBoolean(), Mockito.any(Consumer.class))).thenReturn(Mockito.anyString());
        new RabbitMqEventConsumer(config1);
        new RabbitMqEventConsumer(config2);
        new RabbitMqEventConsumer(config3);
    }

    @Test
    public void consumeZeroRecords() throws Exception {
       	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
       	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	Mockito.when(mockChannel.basicConsume(Mockito.any(), Mockito.anyBoolean(), Mockito.any(Consumer.class))).thenReturn(Mockito.anyString());
       	RabbitMqEventConsumer consumer = new RabbitMqEventConsumer(config1);
        consumer.consume();
        consumer.consumeWithOffsets();
        consumer.consumeAndCommit();
        consumer.close();
    }
    
    @Test
    public void commitOffsets() throws Exception {
       	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
       	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	Mockito.when(mockChannel.basicConsume(Mockito.any(), Mockito.anyBoolean(), Mockito.any(Consumer.class))).thenReturn(Mockito.anyString());
       	RabbitMqEventConsumer consumer = new RabbitMqEventConsumer(config1);
       	consumer.commitOffsets();
        consumer.commitOffsets(0L);
        consumer.close();
    }
    
    @Test
    public void consumeMultipleRecords() throws Exception {
       	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
       	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	Mockito.when(mockChannel.basicConsume(Mockito.any(), Mockito.anyBoolean(), Mockito.any(Consumer.class))).thenReturn(Mockito.anyString());
       	List<MessageWithOffset> records = buildTestMessages(2);
       	mqueue = new ArrayBlockingQueue<>(2);
       	mqueue.addAll(records);
       	RabbitMqEventConsumer consumer = new RabbitMqEventConsumer(config1);
       	consumer.setMessageQueue(mqueue);
       	consumer.consumeAndCommit();
       	consumer.close();
    }
    
    @Test
    public void consumeWithOffSetsMultipleRecords() throws Exception {
       	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
       	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	Mockito.when(mockChannel.basicConsume(Mockito.any(), Mockito.anyBoolean(), Mockito.any(Consumer.class))).thenReturn(Mockito.anyString());
       	List<MessageWithOffset> records = buildTestMessages(2);
       	mqueue = new ArrayBlockingQueue<>(2);
       	mqueue.addAll(records);
       	RabbitMqEventConsumer consumer = new RabbitMqEventConsumer(config1);
       	consumer.setMessageQueue(mqueue);
       	consumer.consumeWithOffsets();
       	consumer.commitOffsets();
        consumer.close();
    }
    
    private List<MessageWithOffset> buildTestMessages(int nbrOfMessages) {
        List<MessageWithOffset> msgList = new ArrayList<MessageWithOffset>();
        for(int i=0;i<nbrOfMessages;i++) {
            MessageWithOffset message = new MessageWithOffset(i,"Message:"+i);
            msgList.add(message);
        }
        return msgList;
    }
}
