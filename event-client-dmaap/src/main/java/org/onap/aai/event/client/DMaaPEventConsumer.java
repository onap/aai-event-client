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
/**
 *
 */
package org.onap.aai.event.client;

import com.att.nsa.mr.client.impl.MRConsumerImpl;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import javax.naming.OperationNotSupportedException;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventConsumer;
import org.onap.aai.event.api.MessageWithOffset;

/**
 * Event Bus Client consumer API that uses AAF authentication with Username/Password.
 */
public class DMaaPEventConsumer implements EventConsumer {

    public static final String DEFAULT_TRANSPORT_TYPE = "HTTPAAF";
    public static final String DEFAULT_PROTOCOL = "http";
    public static final int DEFAULT_MESSAGE_WAIT_TIMEOUT = 15000;
    public static final int DEFAULT_MESSAGE_LIMIT = 1000;

    private static final String OFFSET_UNSUPPORTED = "DMaaP does not support consuming with offsets.";

    private static Logger log = LoggerFactory.getInstance().getLogger(DMaaPEventConsumer.class);

    public interface MRConsumerFactory {
        public MRConsumerImpl createConsumer(List<String> hosts, String topic, String consumerGroup, String consumerId,
                int timeoutMs, int messageLimit, String filter, String username, String password)
                throws MalformedURLException;
    }

    private static MRConsumerFactory consumerFactory = MRConsumerImpl::new;

    private MRConsumerImpl consumer;

    /**
     * Replace the consumer factory (intended to be used for testing purposes only).
     * 
     * @param consumerFactory
     */
    static void setConsumerFactory(MRConsumerFactory consumerFactory) {
        DMaaPEventConsumer.consumerFactory = consumerFactory;
    }

    /**
     * Creates a new instance of DMaaPEventConsumerAAF using supplied parameters.
     *
     * @param host The host and port of the DMaaP server in the format <b>host:port</b>
     * @param topic The topic name to consume from
     * @param username The username of the client application
     * @param password The password for the username
     * @param consumerGroup The consumer group to consume from
     * @param consumerId The consumer ID of the client
     * @param timeoutMs Time in ms to wait for messages on the server before returning
     * @param messageLimit Maximum number of messages that is returned per fetch
     * @param transportType Specifies the request header type used in the request to DMaaP server.<br>
     *        Valid types:
     *        <li>DME2</li>
     *        <li>HTTPAAF</li>
     *        <li>HTTPAUTH</li>
     *        <li>HTTPNOAUTH</li>
     * @param protocol The http protocol to use (http/https)
     * @param filter A customizable message filter, or null if no filtering is required
     * @throws MalformedURLException
     */
    public DMaaPEventConsumer(String host, String topic, String username, String password, String consumerGroup,
            String consumerId, int timeoutMs, int messageLimit, String transportType, String protocol, String filter)
            throws MalformedURLException {
        consumer = consumerFactory.createConsumer(MRConsumerImpl.stringToList(host), topic, consumerGroup, consumerId,
                timeoutMs, messageLimit, filter, username, password);
        consumer.setHost(host);
        consumer.setUsername(username);
        consumer.setPassword(password);
        consumer.setProtocolFlag(transportType);

        // MRConsumerImpl still needs extra properties from the prop object.
        Properties extraProps = new Properties();
        extraProps.put("Protocol", protocol);
        consumer.setProps(extraProps);
    }

    /**
     * Creates a new instance of DMaaPEventConsumerAAF using supplied parameters and default values.
     *
     * @param host The host and port of the DMaaP server in the format <b>host:port</b>
     * @param topic The topic name to consume from
     * @param username The username of the client application
     * @param password The password for the username
     * @param consumerGroup The consumer group to consume from
     * @param consumerId The consumer ID of the client
     * @param timeoutMs Time in ms to wait for messages on the server before returning
     * @param messageLimit Maximum number of messages that is returned per fetch
     * @param transportType Specifies the request header type used in the request to DMaaP server.<br>
     *        Valid types:
     *        <li>DME2</li>
     *        <li>HTTPAAF</li>
     *        <li>HTTPAUTH</li>
     *        <li>HTTPNOAUTH</li>
     * @throws MalformedURLException
     */
    public DMaaPEventConsumer(String host, String topic, String username, String password, String consumerGroup,
            String consumerId, int timeoutMs, int messageLimit, String transportType) throws MalformedURLException {
        this(host, topic, username, password, consumerGroup, consumerId, timeoutMs, messageLimit, transportType,
                DEFAULT_PROTOCOL, null);
    }

    /**
     * Creates a new instance of DMaaPEventConsumerAAF using supplied parameters and default values.
     *
     * @param host The host and port of the DMaaP server in the format <b>host:port</b>
     * @param topic The topic name to consume from
     * @param username The username of the client application
     * @param password The password for the username
     * @param consumerGroup The consumer group to consume from
     * @param consumerId The consumer ID of the client
     * @throws MalformedURLException
     */
    public DMaaPEventConsumer(String host, String topic, String username, String password, String consumerGroup,
            String consumerId) throws MalformedURLException {
        this(host, topic, username, password, consumerGroup, consumerId, DEFAULT_MESSAGE_WAIT_TIMEOUT,
                DEFAULT_MESSAGE_LIMIT, DEFAULT_TRANSPORT_TYPE, DEFAULT_PROTOCOL, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventConsumer#consumeAndCommit()
     */
    @Override
    public Iterable<String> consumeAndCommit() throws Exception {
        log.debug("Querying Event Bus for messages.");
        return consumer.fetch();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventConsumer#consume()
     */
    @Override
    public Iterable<String> consume() throws Exception {
        return consumeAndCommit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventConsumer#consumeWithOffsets()
     */
    @Override
    public Iterable<MessageWithOffset> consumeWithOffsets() throws Exception {
        throw new OperationNotSupportedException(OFFSET_UNSUPPORTED);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventConsumer#commitOffsets()
     */
    @Override
    public void commitOffsets() throws Exception {
        throw new OperationNotSupportedException(OFFSET_UNSUPPORTED);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventConsumer#commitOffsets(long)
     */
    @Override
    public void commitOffsets(long offset) throws Exception {
        throw new OperationNotSupportedException(OFFSET_UNSUPPORTED);
    }

}
