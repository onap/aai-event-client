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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;
import javax.naming.ServiceUnavailableException;

import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventConsumer;
import org.onap.aai.event.api.MessageWithOffset;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;

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

    private BlockingQueue<MessageWithOffset> messageQueue;

    private Connection connection;
    private Channel channel;
    private Long lastDeliveryTag;
    private long timeout = 5000;
    private RabbitMqClientConfig config;
    private long lastConnectionAttempt = 0L; 

    /**
     * (intended for testing prupose only)
     * @param messageQueue
     */
    public void setMessageQueue(BlockingQueue<MessageWithOffset> messageQueue) {
        this.messageQueue = messageQueue;
    }

    public RabbitMqEventConsumer(RabbitMqClientConfig config) throws Exception {
        this.config = config;
        this.messageQueue = new ArrayBlockingQueue<>(1000);

        if (config.getQueue() == null) {
            throw new ConfigurationException("Mandatory config param queue not set");
        }

        try {
            createConnection();
        }
        catch (ConfigurationException ex) {
            // If the configuration is bad, we may as well re-throw the exception and let the process die.
            throw ex;
        }
        catch (Exception ex) {
            // Otherwise, let the process live.  We can retry establishing a connection later.
            log.error(RabbitMqApplicationMsgs.MESSAGE_ERROR, "Unable to connect to RMQ: " + ex.getMessage());
            return;
        }
    }

    private synchronized void createConnection() throws Exception {
        if (connection != null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if ( (currentTime - config.getRetryInterval()) < lastConnectionAttempt) {
            log.warn(RabbitMqApplicationMsgs.MESSAGE_WARN, "No attempt to consume.  No connection to RMQ.");
            throw new ServiceUnavailableException("Waiting for retry interval");
        }

        lastConnectionAttempt = currentTime;

        try {
            this.connection = RabbitMqUtils.createConnection(config);
            this.channel = connection.createChannel();

            if (config.getExchangeName() != null) {
                channel.exchangeDeclare(config.getExchangeName(), config.getExchangeType(), true, false, config.getExchangeArguments());
                channel.queueDeclare(config.getQueue(), true, false, false, config.getQueueArguments());
                channel.queueBind(config.getQueue(), config.getExchangeName(), config.getBindingKey());
            }
            else {
                channel.queueDeclare(config.getQueue(), true, false, false, config.getQueueArguments());
            }

            channel.basicConsume(config.getQueue(), false, new CallBackConsumer(channel)); //AutoAck is false
        }
        catch (Exception ex) {
            close();
            throw ex;
        }

        log.info(RabbitMqApplicationMsgs.MESSAGE_INFO, "Event consumer successfully connected to RMQ");
    }

    @Override
    public Iterable<String> consumeAndCommit() throws Exception {
        Iterable<String> list = consume();
        commitOffsets();
        return list;
    }

    @Override
    public Iterable<String> consume() throws Exception {
        if (connection == null) {
            createConnection();
        }

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
        if (connection == null) {
            createConnection();
        }

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
        if (connection == null) {
            createConnection();
        }

        if(lastDeliveryTag != null)
        {
            channel.basicAck(lastDeliveryTag, true); //Ack messages upto lastDeliveryTag or offset so that they can be marked
            lastDeliveryTag = null;
        }
    }

    @Override
    public void commitOffsets(long offset) throws Exception {
        if (connection == null) {
            createConnection();
        }

        channel.basicAck(offset, true); //Ack messages upto lastDeliveryTag or offset so that they can be marked
    }

    /**
     * Closes the channel
     * @throws Exception
     */
    public void close() throws Exception {
        if (channel != null) {
            channel.close();
            channel = null;
        }

        if (connection != null) {
            connection.close();
            connection = null;
        }
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
