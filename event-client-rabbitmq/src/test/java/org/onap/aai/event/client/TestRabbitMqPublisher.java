package org.onap.aai.event.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

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
