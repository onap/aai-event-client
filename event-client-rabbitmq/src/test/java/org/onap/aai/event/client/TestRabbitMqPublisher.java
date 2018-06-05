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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@RunWith(MockitoJUnitRunner.class)
public class TestRabbitMqPublisher {
    @Mock
    public ConnectionFactory mockConnectionFactory;
    
    @Mock
    public Connection mockConnection;
    
    @Mock
    public Channel mockChannel;
    
    @Mock
    public DeclareOk mockDeclareOK;
    
    BuiltinExchangeType topicEx = BuiltinExchangeType.TOPIC;
    Map<String, Integer> hostPortMap = new HashMap<String, Integer>();

    @Before
    public void init() throws Exception {
    	RabbitMqEventPublisher.setConnectionFactory(mockConnectionFactory);
    }

    @Test
    public void testConstructors() throws Exception {
    	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
    	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
        new RabbitMqEventPublisher("hosts", 0, "userName", "password", "exchangeName");
        new RabbitMqEventPublisher("hosts", 0, "userName", "password", BuiltinExchangeType.DIRECT.name(), "exchangeName");
        new RabbitMqEventPublisher(hostPortMap, "userName", "password", "exchangeName");
        new RabbitMqEventPublisher(hostPortMap, "userName", "password", BuiltinExchangeType.DIRECT.name(), "exchangeName");
        new RabbitMqEventPublisher("hosts", 0, "userName", "password", "queueName",new HashMap<String, Object>());
    }
    

    @Test
    public void publishSynchronous() throws Exception {
    	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
    	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
    	RabbitMqEventPublisher publisher = new RabbitMqEventPublisher("hosts", 0, "userName", "password", "exchangeName");
        publisher.sendSync("");
        publisher.sendSync(Arrays.asList(""));
        publisher.sendSync("key", "");
        publisher.sendSync("key", Arrays.asList(""));
        publisher.close();
    }

    @Test
    public void publishAsynchronous() throws Exception {
    	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
    	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
    	RabbitMqEventPublisher publisher = new RabbitMqEventPublisher("hosts", 0, "userName", "password", "exchangeName");
        publisher.sendAsync("");
        publisher.sendAsync(Arrays.asList(""));
        publisher.sendAsync("key", "");
        publisher.sendAsync("key", Arrays.asList(""));
        publisher.close();
    } 
    
    @Test(expected = UnsupportedOperationException.class)
    public void UnsupportedMessageTest() throws Exception {
    	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
    	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	RabbitMqEventPublisher publisher = new RabbitMqEventPublisher("hosts", 0, "userName", "password", "queueName",new HashMap<String, Object>());
        publisher.sendSync("key", "");
        publisher.sendSync("key", Arrays.asList(""));
        publisher.close();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void UnSupportedMultiMessageTests() throws Exception {
    	Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
    	Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
       	Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
       	RabbitMqEventPublisher publisher = new RabbitMqEventPublisher("hosts", 0, "userName", "password", "queueName",new HashMap<String, Object>());
        publisher.sendSync("key", Arrays.asList(""));
        publisher.close();
    }
    
}
