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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventConsumer;
import org.onap.aai.event.api.MessageWithOffset;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;

/**
 * Event bus client consumer wrapper for RabbitMQ.
 * This will open a async consumer which will put messages on internal queue so that calling client can then
 * consume and ackowledge the same based on offset/deliveryTag.
 * Topic exchange type is powerful and can behave like other exchanges.
 * When a queue is bound with "#" (hash) binding key - it will receive all the messages, regardless of the routing key - like in fanout exchange.
 * When special characters "*" (star) and "#" (hash) aren't used in bindings, the topic exchange will behave just like a direct one
 * @author GURJEETB
 *
 */
public class RabbitMqEventConsumer implements EventConsumer {
	private static Logger log = LoggerFactory.getInstance().getLogger(RabbitMqEventConsumer.class);
	private static ConnectionFactory factory = new ConnectionFactory();
	private BlockingQueue<MessageWithOffset> messageQueue;

	private static String BINDING_CONSUME_ALL = "#";

	private final Connection connection;
	private final Channel channel;
	private final String queueName;
	private DeclareOk exchangeInfo;
	private Long lastDeliveryTag;
	private long timeout = 5000;
	private com.rabbitmq.client.AMQP.Queue.DeclareOk queueInfo;
	
	/**
	 * (intended for testing prupose only)
	 * @param connFactory
	 */
    static void setConnectionFactory(ConnectionFactory connFactory) {
    	factory = connFactory;
    }
    
    /**
     * (intended for testing prupose only)
     * @param messageQueue
     */
	public void setMessageQueue(BlockingQueue<MessageWithOffset> messageQueue) {
		this.messageQueue = messageQueue;
	}

	/**
	 * Constructor to open a consumer on single host port for a topic exchange with specific queue name which will 
	 * consume all messages from topic
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param exchangeName
	 * @param queueName
	 * @throws Exception
	 */
	public RabbitMqEventConsumer(String host, int port, String userName, String password, String exchangeName, String queueName) throws Exception {
		this(host, port, userName, password, BuiltinExchangeType.TOPIC.name(), exchangeName, queueName, BINDING_CONSUME_ALL);
	}
	
	/**
	 * Constructor to open a consumer on single host port for a exchange with specific queue name which will 
	 * consume all messages from topic
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param exchangeType  - Supported values - DIRECT, FANOUT, TOPIC, HEADERS
	 * @param exchangeName
	 * @param queueName
	 * @throws Exception
	 */
	public RabbitMqEventConsumer(String host, int port, String userName, String password, String exchangeType, String exchangeName, String queueName)  throws Exception {
		this(host, port, userName, password, exchangeType, exchangeName, queueName, BINDING_CONSUME_ALL);
	}
	
	/**
	 * Constructor to open a consumer on single host port for a exchange with specific queue name 
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param exchangeType
	 * @param exchangeName
	 * @param queueName
	 * @param bindingKey - Bind the queue to specific messages only
	 * @throws Exception
	 */
	public RabbitMqEventConsumer(String host, int port, String userName, String password, String exchangeType, String exchangeName, String queueName, String bindingKey)  throws Exception {
		this(buildMap(host, port), userName, password, BuiltinExchangeType.valueOf(exchangeType), exchangeName, queueName, bindingKey, new HashMap<String, Object>());
	}
	
	/**
	 * Constructor to open a consumer on multiple host port for a topic exchange with specific queue name which will 
	 * consume all messages from topic
	 * @param hostPortMap
	 * @param userName
	 * @param password
	 * @param exchangeName
	 * @param queueName
	 * @throws Exception
	 */
	public RabbitMqEventConsumer(Map<String, Integer> hostPortMap, String userName, String password, String exchangeName, String queueName) throws Exception {
		this(hostPortMap, userName, password, BuiltinExchangeType.TOPIC.name(), exchangeName, queueName, BINDING_CONSUME_ALL);
	}
	
	/**
	 * Constructor to open a consumer on multiple host port for a exchange with specific queue name which will 
	 * consume all messages from topic
	 * @param hostPortMap
	 * @param userName
	 * @param password
	 * @param exchangeType  - Supported values - DIRECT, FANOUT, TOPIC, HEADERS
	 * @param exchangeName
	 * @param queueName
	 * @throws Exception
	 */
	public RabbitMqEventConsumer(Map<String, Integer> hostPortMap, String userName, String password, String exchangeType, String exchangeName, String queueName)  throws Exception {
		this(hostPortMap, userName, password, exchangeType, exchangeName, queueName, BINDING_CONSUME_ALL);
	}
	
	/**
	 * Constructor to open a consumer on multiple host port for a exchange with specific queue name 
	 * @param hostPortMap
	 * @param userName
	 * @param password
	 * @param exchangeType
	 * @param exchangeName
	 * @param queueName
	 * @param bindingKey  - Bind the queue to specific messages only
	 * @throws Exception
	 */
	public RabbitMqEventConsumer(Map<String, Integer> hostPortMap, String userName, String password, String exchangeType, String exchangeName, String queueName, String bindingKey)  throws Exception {
		this(hostPortMap, userName, password, BuiltinExchangeType.valueOf(exchangeType), exchangeName, queueName, bindingKey, new HashMap<String, Object>());
	}		
	
	public RabbitMqEventConsumer(Map<String, Integer> hostPortMap, String userName, String password, BuiltinExchangeType exchangeType, String exchangeName, String queueName, String bindingKey, Map<String, Object> exchangeArguments) throws Exception {
		messageQueue = new ArrayBlockingQueue<>(1000);
		List<Address> addresses = new ArrayList<Address>();
		Iterator<String> iter = hostPortMap.keySet().iterator();
		while (iter.hasNext())
		{
			String host = iter.next();
			int port = hostPortMap.get(host);
			Address add = new Address(host,port);
			addresses.add(add);
		}
		factory.setUsername(userName);
		factory.setPassword(password);
		connection = factory.newConnection(addresses);
		channel = connection.createChannel();
		exchangeInfo = channel.exchangeDeclare(exchangeName, exchangeType, true, false, exchangeArguments);
		this.queueName = queueName;
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, exchangeName, bindingKey);
	    String consumerTag = channel.basicConsume(queueName, false, new CallBackConsumer(channel)); //AutoAck is false
	}
	
	public RabbitMqEventConsumer(String host, int port, String userName, String password, String queue) throws Exception {
		this(buildMap(host, port), userName, password, queue, new HashMap<String, Object>());
	}
	
	public RabbitMqEventConsumer(Map<String, Integer> hostPortMap, String userName, String password, String queue, Map<String, Object> queueArguments) throws Exception {
		messageQueue = new ArrayBlockingQueue<>(1000);
		List<Address> addresses = new ArrayList<Address>();
		Iterator<String> iter = hostPortMap.keySet().iterator();
		while (iter.hasNext())
		{
			String host = iter.next();
			int port = hostPortMap.get(host);
			Address add = new Address(host,port);
			addresses.add(add);
		}
		factory.setUsername(userName);
		factory.setPassword(password);
		connection = factory.newConnection(addresses);
		channel = connection.createChannel();
		this.queueName = queue;
		queueInfo = channel.queueDeclare(queueName, true, false, false, queueArguments);
	    String consumerTag = channel.basicConsume(queueName, false, new CallBackConsumer(channel)); //AutoAck is false
	}
	
	private static Map<String, Integer> buildMap(String host, Integer port) {
		Map<String, Integer> hostPortMap = new HashMap<String, Integer>();
		hostPortMap.put(host, port);
		return hostPortMap;
	}	
	
	@Override
	public Iterable<String> consumeAndCommit() throws Exception {
		Iterable<String> list = consume();
		commitOffsets();
		return list;
	}

	@Override
	public Iterable<String> consume() throws Exception {
		List<String> list = new ArrayList<>();
		MessageWithOffset record = null;
		if(messageQueue.peek()!=null) {
			do
			{
				record = messageQueue.poll(1000, TimeUnit.MILLISECONDS);
				lastDeliveryTag = record.getOffset();
				list.add(record.getMessage());
			}while(messageQueue.peek()!=null);
		}
		return list;
	}

	@Override
	public Iterable<MessageWithOffset> consumeWithOffsets() throws Exception {
		List<MessageWithOffset> list = new ArrayList<>();
		MessageWithOffset record = null;
		if(messageQueue.peek()!=null) {
			do
			{
				record = messageQueue.poll(1000, TimeUnit.MILLISECONDS);
				lastDeliveryTag = record.getOffset();
				list.add(record);
			}while(messageQueue.peek()!=null);
		}
        return list;
	}

	@Override
	public void commitOffsets() throws Exception {
		if(lastDeliveryTag != null)
		{
			channel.basicAck(lastDeliveryTag, true); //Ack messages upto lastDeliveryTag or offset so that they can be marked
			lastDeliveryTag = null;
		}
	}

	@Override
	public void commitOffsets(long offset) throws Exception {
		channel.basicAck(offset, true); //Ack messages upto lastDeliveryTag or offset so that they can be marked
	}
	
	/**
	 * Closes the channel
	 * @throws Exception
	 */
	public void close() throws Exception {
		channel.close();
		connection.close();
	}
	
	class CallBackConsumer extends DefaultConsumer{
		CallBackConsumer(Channel channel) {
			super(channel);
		}
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
	                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
	       String message = new String(body, "UTF-8");
	       try
	       {
	    	   MessageWithOffset record = new MessageWithOffset(envelope.getDeliveryTag(), message);
	    	   messageQueue.offer(record, timeout, TimeUnit.MILLISECONDS);
	       }
	       catch(Exception e)
	       {
	    	   log.debug(" Got exception while handling message="+e.getMessage()+" Will be reposting to queue");
		       channel.basicNack(envelope.getDeliveryTag(), false, true); //Explicit Ack with requeue
	       }
	    }
	}
}
