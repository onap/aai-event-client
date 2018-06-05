/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 European Software Marketing Ltd.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventPublisher;

import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Event bus client publisher wrapper for RabbitMQ
 * @author GURJEETB
 *
 */

public class RabbitMqEventPublisher implements EventPublisher {
	private static Logger log = LoggerFactory.getInstance().getLogger(RabbitMqEventPublisher.class);
	private static ConnectionFactory factory = new ConnectionFactory();

	private final Connection connection;
	private final Channel channel;
	private final String exchangeName;
	private final String queueName;
	private final String routingKey;
	private DeclareOk exchangeInfo; 
	private com.rabbitmq.client.AMQP.Queue.DeclareOk queueInfo;

    static void setConnectionFactory(ConnectionFactory connFactory) {
    	factory = connFactory;
    }
	/**
	 * Constructor for single host port for a topic exchange
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param exchangeName
	 * @throws Exception
	 */
	public RabbitMqEventPublisher(String host, int port, String userName, String password, String exchangeName) throws Exception {
		this(host, port, userName, password, BuiltinExchangeType.TOPIC.name(), exchangeName,"");
	}
	
	/**
	 * Constructor for single host port for a specific exchange type
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param exchangeType - Supported values - DIRECT, FANOUT, TOPIC, HEADERS
	 * @param exchangeName
	 * @throws Exception
	 */
	public RabbitMqEventPublisher(String host, int port, String userName, String password, String exchangeType, String exchangeName) throws Exception {
		this(host,port, userName, password, exchangeType, exchangeName,"");
	}
	
	/**
	 * Constructor for single host port for a specific exchange type and specific routing key
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param exchangeType
	 * @param exchangeName
	 * @param routingKey
	 * @throws Exception
	 */
	public RabbitMqEventPublisher(String host, int port, String userName, String password, String exchangeType, String exchangeName, String routingKey) throws Exception {
		this(buildMap(host,port), userName, password, BuiltinExchangeType.valueOf(exchangeType), exchangeName,routingKey, new HashMap<String, Object>());
	}
	
	/**
	 * Constructor for multiple host and port for a topic exchange
	 * @param hostPortMap
	 * @param userName
	 * @param password
	 * @param exchangeName
	 * @throws Exception
	 */
	public RabbitMqEventPublisher(Map<String, Integer> hostPortMap, String userName, String password, String exchangeName) throws Exception {
		this(hostPortMap, userName, password, BuiltinExchangeType.TOPIC.name(), exchangeName,"");
	}
	
	/**
	 * Constructor for multiple host port for a specific exchange type
	 * @param hostPortMap
	 * @param userName
	 * @param password
	 * @param exchangeType - Supported values - DIRECT, FANOUT, TOPIC, HEADERS
	 * @param exchangeName
	 * @throws Exception
	 */
	public RabbitMqEventPublisher(Map<String, Integer> hostPortMap, String userName, String password, String exchangeType, String exchangeName) throws Exception {
		this(hostPortMap, userName, password, exchangeType , exchangeName,"");
	}
	
	/**
	 * Constructor for multiple host port for a specific exchange type and specific routing key
	 * @param hostPortMap
	 * @param userName
	 * @param password
	 * @param exchangeType
	 * @param exchangeName
	 * @param routingKey
	 * @throws Exception
	 */
	public RabbitMqEventPublisher(Map<String, Integer> hostPortMap, String userName, String password, String exchangeType, String exchangeName, String routingKey) throws Exception {
		this(hostPortMap, userName, password, BuiltinExchangeType.valueOf(exchangeType), exchangeName,routingKey, new HashMap<String, Object>());
	}

	public RabbitMqEventPublisher(Map<String, Integer> hostPortMap, String userName, String password, BuiltinExchangeType exchangeType, String exchangeName, String routingKey, Map<String, Object> exchangeArguments) throws Exception {
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
		this.exchangeName = exchangeName;
		exchangeInfo = channel.exchangeDeclare(exchangeName, exchangeType, true, false, exchangeArguments); //Durable exchange and non delete
		queueName = null;
		this.routingKey=routingKey;
	}
	
	/**
	 * Constructor for single host port for a queue connection
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param queue
	 * @param queueArguments
	 * @throws Exception
	 */
	public RabbitMqEventPublisher(String host, int port, String userName, String password, String queue, Map<String, Object> queueArguments) throws Exception {
		this(buildMap(host, port), userName, password, queue, queueArguments);
	}
	
	/**
	 * Constructor for multiple host port for a queue connection
	 * @param hostPortMap
	 * @param userName
	 * @param password
	 * @param queue
	 * @param queueArguments
	 * @throws Exception
	 */
	public RabbitMqEventPublisher(Map<String, Integer> hostPortMap, String userName, String password, String queue, Map<String, Object> queueArguments) throws Exception {
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
		exchangeName = "";
		routingKey=queue;
		this.queueName = queue;
		queueInfo = channel.queueDeclare(queueName, true, false, false, queueArguments); //Durable, non exclusive and non auto delete queue
	}
	
	private static Map<String, Integer> buildMap(String host, Integer port) {
		Map<String, Integer> hostPortMap = new HashMap<String, Integer>();
		hostPortMap.put(host, port);
		return hostPortMap;
	}

	@Override
	public void close() throws Exception {
		channel.close();
		connection.close();
	}

	@Override
	public void sendAsync(String message) throws Exception {
		sendSync(message);
	}

	@Override
	public void sendAsync(Collection<String> messages) throws Exception {
		sendSync(messages);
	}

	@Override
	public void sendAsync(String routingParam, String message) throws Exception {
		sendSync(routingParam, message);
	}

	@Override
	public void sendAsync(String routingParam, Collection<String> messages) throws Exception {
		sendSync(routingParam, messages);
	}

	@Override
	public int sendSync(String message) throws Exception {
		channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
		log.debug(" [x] Sent '" + message + "'");		
		return 1;
	}

	@Override
	public int sendSync(Collection<String> messages) throws Exception {
		log.debug("Publishing" + messages.size() + " messages ");
        for (String message : messages) {
            sendSync(message);
        }
        return messages.size();		
	}

	@Override
	public int sendSync(String routingParam, String message) throws Exception {
		//Can only route if exchange is setup
		if(queueName == null) {
			channel.basicPublish(exchangeName, routingParam, null, message.getBytes());
			return 1;
		}
		else
			throw new UnsupportedOperationException("Routing without exchange");
			
	}

	@Override
	public int sendSync(String routingParam, Collection<String> messages) throws Exception {
		log.debug("Publishing" + messages.size() + " messages ");
		//Can only route if exchange is setup
		if(queueName == null) {
	        for (String message : messages) {
	            sendSync(routingParam, message);
	        }
	        return messages.size();
		}
		else
			throw new UnsupportedOperationException("Routing without exchange");
	}

}
