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
        
        config2.setHosts("host1:1234");
        config2.setUsername("user");
        config2.setPassword("secret");
        config2.setExchangeName("my-exchange");
        config2.setExchangeType(BuiltinExchangeType.DIRECT.name());
    }

    @Test
    public void testConstructors() throws Exception {
        Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
        Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
        Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
        new RabbitMqEventPublisher(config1);
        new RabbitMqEventPublisher(config2);
    }


    @Test
    public void publishSynchronous() throws Exception {
        Mockito.when(mockConnectionFactory.newConnection(Mockito.anyListOf(Address.class))).thenReturn(mockConnection);
        Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
        Mockito.when(mockChannel.exchangeDeclare(Mockito.any(), Mockito.eq(topicEx), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(mockDeclareOK);
        RabbitMqEventPublisher publisher = new RabbitMqEventPublisher(config1);
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
        RabbitMqEventPublisher publisher = new RabbitMqEventPublisher(config1);
        publisher.sendAsync("");
        publisher.sendAsync(Arrays.asList(""));
        publisher.sendAsync("key", "");
        publisher.sendAsync("key", Arrays.asList(""));
        publisher.close();
    }
}
