/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017 European Software Marketing Ltd.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
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
    
    @Before
    public void init() throws Exception {
    	RabbitMqEventConsumer.setConnectionFactory(mockConnectionFactory);
    }

    @Test
    public void testConstructor() throws Exception {
       	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
    	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	Mockito.when(mockChannel.basicConsume(Mockito.any(), Mockito.anyBoolean(), Mockito.any(Consumer.class))).thenReturn(Mockito.anyString());
        new RabbitMqEventConsumer("hosts", 0, "userName", "password", "exchangeName", "queueName");
        new RabbitMqEventConsumer("hosts", 0, "userName", "password", BuiltinExchangeType.DIRECT.name(), "exchangeName", "queueName");
        new RabbitMqEventConsumer(hostPortMap, "userName", "password", "exchangeName", "queueName");
        new RabbitMqEventConsumer(hostPortMap, "userName", "password", BuiltinExchangeType.DIRECT.name(), "exchangeName", "queueName");
        new RabbitMqEventConsumer("hosts", 0, "userName", "password", "queue");      
    }

    @Test
    public void consumeZeroRecords() throws Exception {
       	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
    	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	Mockito.when(mockChannel.basicConsume(Mockito.any(), Mockito.anyBoolean(), Mockito.any(Consumer.class))).thenReturn(Mockito.anyString());
    	RabbitMqEventConsumer consumer = new RabbitMqEventConsumer("hosts", 0, "userName", "password", "exchangeName", "queueName");
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
    	RabbitMqEventConsumer consumer = new RabbitMqEventConsumer("hosts", 0, "userName", "password", "exchangeName", "queueName");
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
       	RabbitMqEventConsumer consumer = new RabbitMqEventConsumer("hosts", 0, "userName", "password", "exchangeName", "queueName");
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
       	RabbitMqEventConsumer consumer = new RabbitMqEventConsumer("hosts", 0, "userName", "password", "exchangeName", "queueName");
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
