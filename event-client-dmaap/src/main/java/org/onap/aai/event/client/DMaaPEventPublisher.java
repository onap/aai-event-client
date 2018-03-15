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

import com.att.nsa.mr.client.MRPublisher.message;
import com.att.nsa.mr.client.impl.MRConsumerImpl;
import com.att.nsa.mr.client.impl.MRSimplerBatchPublisher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventPublisher;

/**
 * Event Bus Client publisher API that uses AAF authentication with Username/Password.
 */
public class DMaaPEventPublisher implements EventPublisher {

    public static final String DEFAULT_TRANSPORT_TYPE = "HTTPAAF";
    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_CONTENT_TYPE = "application/json";
    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final long DEFAULT_BATCH_AGE = 250;
    public static final int DEFAULT_BATCH_DELAY = 50;
    public static final String DEFAULT_PARTITION = "0";
    public static final int CLOSE_TIMEOUT = 20;

    private static final String ASYNC_UNSUPPORTED =
            "This implementation of EventPublisher does not support async mode.";

    private static Logger log = LoggerFactory.getInstance().getLogger(DMaaPEventPublisher.class);

    public interface MRPublisherFactory {
        public MRSimplerBatchPublisher createPublisher(String host, String topic, int maxBatchSize, long maxAgeMs,
                int delayBetweenBatchesMs);
    }

    private static MRPublisherFactory publisherFactory = DMaaPEventPublisher::createMRSimplerBatchPublisher;

    private MRSimplerBatchPublisher publisher;

    /**
     * Replace the publisher factory (intended to be used for testing purposes only).
     * 
     * @param publisherFactory
     */
    static void setPublisherFactory(MRPublisherFactory publisherFactory) {
        DMaaPEventPublisher.publisherFactory = publisherFactory;
    }

    /**
     * Provide the default factory method so that test code is able to restore this functionality.
     * 
     * @return the default publisher factory implementation
     */
    static MRPublisherFactory getPublisherFactory() {
        return publisherFactory;
    }

    /**
     * Creates a new instance of MRSimplerBatchPublisher using the supplied parameters.
     * 
     * @param host The host and port of the DMaaP server in the format <b>host:port</b>
     * @param topic The name of the topic to which messages are published
     * @param maxBatchSize The maximum batch size for each send operation
     * @param maxAgeMs The maximum age of each batch before sending
     * @param delayBetweenBatchesMs Time to wait between sending each batch
     * @return a new MRSimplerBatchPublisher object
     */
    private static MRSimplerBatchPublisher createMRSimplerBatchPublisher(String host, String topic, int maxBatchSize,
            long maxAgeMs, int delayBetweenBatchesMs) {
        return new MRSimplerBatchPublisher.Builder().againstUrls(MRConsumerImpl.stringToList(host)).onTopic(topic)
                .batchTo(maxBatchSize, maxAgeMs).httpThreadTime(delayBetweenBatchesMs).build();
    }

    /**
     * Creates a new instance of DMaaPEventPublisher using supplied parameters.
     *
     * @param host The host and port of the DMaaP server in the format <b>host:port</b>
     * @param topic The topic name to publish to
     * @param username The username of the client application
     * @param password The password for the username
     * @param maxBatchSize The maximum batch size for each send
     * @param maxAgeMs The max age of each batch in ms before sending
     * @param delayBetweenBatchesMs Time in ms to wait between sending each batch
     * @param transportType Specifies the request header type used in the request to DMaaP server.<br>
     *        Valid types:
     *        <li>DME2</li>
     *        <li>HTTPAAF</li>
     *        <li>HTTPAUTH</li>
     *        <li>HTTPNOAUTH</li>
     * @param protocol The http protocol to use (http/https)
     * @param contentType The content-type request header value (e.g. application/json)
     */
    public DMaaPEventPublisher(String host, String topic, String username, String password, int maxBatchSize,
            long maxAgeMs, int delayBetweenBatchesMs, String transportType, String protocol, String contentType) {
        publisher = getPublisherFactory().createPublisher(host, topic, maxBatchSize, maxAgeMs, delayBetweenBatchesMs);
        publisher.setUsername(username);
        publisher.setPassword(password);
        publisher.setProtocolFlag(transportType);

        // MRSimplerBatchPublisher still needs extra properties from the prop object.
        Properties extraProps = new Properties();
        extraProps.put("Protocol", protocol);
        extraProps.put("contenttype", contentType);
        publisher.setProps(extraProps);
    }

    /**
     * Creates a new instance of DMaapEventPublisherAAF using supplied parameters and default values.
     *
     * @param host The host and port of the DMaaP server in the format <b>host:port</b>
     * @param topic The topic name to publish to
     * @param username The username of the client application
     * @param password The password for the username
     * @param maxBatchSize The maximum batch size for each send
     * @param maxAgeMs The max age of each batch in ms before sending
     * @param delayBetweenBatchesMs Time in ms to wait between sending each batch
     * @param transportType Specifies the request header type used in the request to DMaaP server.<br>
     *        Valid types:
     *        <li>DME2</li>
     *        <li>HTTPAAF</li>
     *        <li>HTTPAUTH</li>
     *        <li>HTTPNOAUTH</li>
     */
    public DMaaPEventPublisher(String host, String topic, String username, String password, int maxBatchSize,
            long maxAgeMs, int delayBetweenBatchesMs, String transportType) {
        this(host, topic, username, password, maxBatchSize, maxAgeMs, delayBetweenBatchesMs, transportType,
                DEFAULT_PROTOCOL, DEFAULT_CONTENT_TYPE);
    }

    /**
     * Creates a new instance of DMaapEventPublisherAAF using supplied parameters and default values.
     *
     * @param host The host and port of the DMaaP server in the format <b>host:port</b>
     * @param topic The topic name to publish to
     * @param username The username of the client application
     * @param password The password for the username
     */
    public DMaaPEventPublisher(String host, String topic, String username, String password) {
        this(host, topic, username, password, DEFAULT_BATCH_SIZE, DEFAULT_BATCH_AGE, DEFAULT_BATCH_DELAY,
                DEFAULT_TRANSPORT_TYPE, DEFAULT_PROTOCOL, DEFAULT_CONTENT_TYPE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#close()
     */
    @Override
    public void close() throws Exception {
        publisher.close(CLOSE_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * Close the publisher and return a list of unsent messages.
     *
     * @return a list of unsent messages.
     * @throws Exception
     */
    public List<String> closeWithUnsent() throws Exception {
        return publisher.close(CLOSE_TIMEOUT, TimeUnit.SECONDS).stream().map(m -> m.fMsg).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#sendAsync(java.lang.String)
     */
    @Override
    public void sendAsync(String message) throws Exception {
        throw new UnsupportedOperationException(ASYNC_UNSUPPORTED);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#sendAsync(java.util.Collection)
     */
    @Override
    public void sendAsync(Collection<String> messages) throws Exception {
        throw new UnsupportedOperationException(ASYNC_UNSUPPORTED);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#sendAsync(java.lang.String, java.lang.String)
     */
    @Override
    public void sendAsync(String partition, String message) throws Exception {
        throw new UnsupportedOperationException(ASYNC_UNSUPPORTED);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#sendAsync(java.lang.String, java.util.Collection)
     */
    @Override
    public void sendAsync(String partition, Collection<String> messages) throws Exception {
        throw new UnsupportedOperationException(ASYNC_UNSUPPORTED);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#sendSync(java.lang.String)
     */
    @Override
    public int sendSync(String message) throws Exception {
        return sendSync(DEFAULT_PARTITION, message);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#sendSync(java.util.Collection)
     */
    @Override
    public int sendSync(Collection<String> messages) throws Exception {
        return sendSync(DEFAULT_PARTITION, messages);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#sendSync(java.lang.String, java.lang.String)
     */
    @Override
    public int sendSync(String partition, String message) throws Exception {
        log.debug("Publishing message on partition " + partition + ": " + message);

        publisher.getProps().put("partition", partition);
        return publisher.send(partition, message);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.event.api.EventPublisher#sendSync(java.lang.String, java.util.Collection)
     */
    @Override
    public int sendSync(String partition, Collection<String> messages) throws Exception {
        log.debug("Publishing " + messages.size() + " messages on partition " + partition);

        publisher.getProps().put("partition", partition);

        Collection<message> dmaapMessages = new ArrayList<>();
        for (String message : messages) {
            dmaapMessages.add(new message(partition, message));
        }
        return publisher.send(dmaapMessages);
    }

}
