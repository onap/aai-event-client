package org.onap.aai.event.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

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
	private static ConnectionFactory factory;

	private final Connection connection;
	private final Channel channel;
	private final String exchangeName;
	private final String queueName;
	private final String routingKey;
	private DeclareOk exchangeInfo; 
	private com.rabbitmq.client.AMQP.Queue.DeclareOk queueInfo;

	private static ConnectionFactory getConnFactoryInstance() {
		if(factory == null)
			factory = new ConnectionFactory();
		return factory;
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
		this(host, port, userName, password, BuiltinExchangeType.TOPIC.getType(), exchangeName,"");
	}
	
	/**
	 * Constructor for single host port for a specific exchange type
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param exchangeType - Supported values - direct, fanout, topic, headers
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
		this(hostPortMap, userName, password, BuiltinExchangeType.TOPIC.getType(), exchangeName,"");
	}
	
	/**
	 * Constructor for multiple host port for a specific exchange type
	 * @param hostPortMap
	 * @param userName
	 * @param password
	 * @param exchangeType - Supported values - direct, fanout, topic, headers
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
		factory = getConnFactoryInstance();
		Address[] addresses = new Address[hostPortMap.size()];
		Iterator<String> iter = hostPortMap.keySet().iterator();
		int i=0;
		while (iter.hasNext())
		{
			String host = iter.next();
			int port = hostPortMap.get(host);
			Address add = new Address(host,port);
			addresses[i++] = add;
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
		factory = getConnFactoryInstance();
		Address[] addresses = new Address[hostPortMap.size()];
		Iterator<String> iter = hostPortMap.keySet().iterator();
		int i=0;
		while (iter.hasNext())
		{
			String host = iter.next();
			int port = hostPortMap.get(host);
			Address add = new Address(host,port);
			addresses[i++] = add;
		}
		factory.setUsername(userName);
		factory.setPassword(password);
		connection = factory.newConnection(addresses);
		channel = connection.createChannel();
		exchangeName = "";
		routingKey=queue;
		this.queueName = queue;
		queueInfo = channel.queueDeclare(queueName, true, false, false, queueArguments);
	}
	
	private static Map<String, Integer> buildMap(String host, Integer port) {
		Map<String, Integer> hostPortMap = new HashMap<String, Integer>();
		hostPortMap.put(host, port);
		return hostPortMap;
	}

	@Override
	public void close() throws Exception {
		if(channel != null)
			channel.close();
		if(connection != null)
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
		if(exchangeName != null) {
			channel.basicPublish(exchangeName, routingParam, null, message.getBytes());
			return 1;
		}
		else
			throw new OperationNotSupportedException("Routing without exchange");
			
	}

	@Override
	public int sendSync(String routingParam, Collection<String> messages) throws Exception {
		log.debug("Publishing" + messages.size() + " messages ");
		if(exchangeName != null) {
	        for (String message : messages) {
	            sendSync(routingParam, message);
	        }
	        return messages.size();
		}
		else
			throw new OperationNotSupportedException("Routing without exchange");
	}

}
