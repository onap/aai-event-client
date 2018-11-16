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

import java.util.Collection;

import javax.naming.ConfigurationException;

import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventPublisher;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Event bus client publisher wrapper for RabbitMQ
 * @author GURJEETB
 *
 */

public class RabbitMqEventPublisher implements EventPublisher {
    private static Logger log = LoggerFactory.getInstance().getLogger(RabbitMqEventPublisher.class);

    private final Connection connection;
    private final Channel channel;

    private RabbitMqClientConfig config;

    public RabbitMqEventPublisher(RabbitMqClientConfig config) throws Exception {
        this.config = config;
        
        if (config.getExchangeName() == null) {
            throw new ConfigurationException("Mandatory config param exchangeName not set");
        }
        
        this.connection = RabbitMqUtils.createConnection(config);
        this.channel = connection.createChannel();

        //Durable exchange and non delete
        channel.exchangeDeclare(config.getExchangeName(), config.getExchangeType(), true, false, config.getExchangeArguments());
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
        channel.basicPublish(config.getExchangeName(), config.getRoutingKey(), null, message.getBytes());
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
        channel.basicPublish(config.getExchangeName(), routingParam, null, message.getBytes());
        return 1;	
    }

    @Override
    public int sendSync(String routingParam, Collection<String> messages) throws Exception {
        log.debug("Publishing" + messages.size() + " messages ");
        for (String message : messages) {
            sendSync(routingParam, message);
        }
        return messages.size();
    }

}
