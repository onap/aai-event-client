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

import java.util.Collection;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventPublisher;

/**
 * Event Bus Client publisher implementation for Kafka .A KafkaProducer that publishes records to the Kafka cluster is
 * thread safe and sharing a single producer instance across threads will generally be faster than having multiple
 * instances. Ref :https://kafka.apache.org/0102/javadoc/index.html?org/apache/kafka/clients/
 * producer/KafkaProducer.html
 *
 *
 */
public class KafkaEventPublisher implements EventPublisher {

    private static Logger log = LoggerFactory.getInstance().getLogger(KafkaEventPublisher.class);

    public interface KafkaProducerFactory {
        public KafkaProducer<String, String> createProducer(Properties props);
    }

    private static final String PUBLISHING = "Publishing ";

    private static KafkaProducerFactory producerFactory = KafkaProducer::new;

    private final KafkaProducer<String, String> producer;
    private final String topic;

    /**
     * Replace the producer factory (intended to be used for testing purposes only).
     * 
     * @param producerFactory
     */
    static void setProducerFactory(KafkaProducerFactory producerFactory) {
        KafkaEventPublisher.producerFactory = producerFactory;
    }

    /**
     *
     * @param hosts - A list of host/port pairs to use for establishing the initial connection to the Kafka cluster. The
     *        client will make use of all servers irrespective of which servers are specified here for
     *        bootstrapping—this list only impacts the initial hosts used to discover the full set of servers. This list
     *        should be in the form host1:port1,host2:port2,....
     * @param topic - Topic to publish the messages to
     * @param bufferMemory - The total bytes of memory the producer can use to buffer records waiting to be sent to the
     *        server
     * @param batchSize - The producer will attempt to batch records together into fewer requests whenever multiple
     *        records are being sent to the same partition. This helps performance on both the client and the server.
     *        This configuration controls the default batch size in bytes
     * @param retries -Setting a value greater than zero will cause the client to resend any record whose send fails
     *        with a potentially transient error. Note that this retry is no different than if the client resent the
     *        record upon receiving the error.
     */
    public KafkaEventPublisher(String hosts, String topic, long bufferMemory, int batchSize, int retries) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = producerFactory.createProducer(props);
        this.topic = topic;

    }

    /**
     *
     * @param hosts - A list of host/port pairs to use for establishing the initial connection to the Kafka cluster. The
     *        client will make use of all servers irrespective of which servers are specified here for
     *        bootstrapping—this list only impacts the initial hosts used to discover the full set of servers. This list
     *        should be in the form host1:port1,host2:port2,....
     * @param topic - Topic to publish the messages to
     */
    public KafkaEventPublisher(String hosts, String topic) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = producerFactory.createProducer(props);
        this.topic = topic;
    }

    /**
     * Closes the publisher.
     */
    @Override
    public void close() {
        producer.close();
    }

    @Override
    public int sendSync(String partitionKey, String message) throws Exception {
        log.debug("Publishing message on partitionKey " + partitionKey + ": " + message);
        producer.send(new ProducerRecord<String, String>(topic, partitionKey, message)).get();
        return 1;
    }

    @Override
    public int sendSync(String partitionKey, Collection<String> messages) throws Exception {
        log.debug(PUBLISHING + messages.size() + " messages on partitionKey " + partitionKey);
        for (String message : messages) {
            sendSync(partitionKey, message);
        }
        return messages.size();
    }

    @Override
    public int sendSync(String message) throws Exception {
        log.debug("Publishing message : " + message);
        producer.send(new ProducerRecord<String, String>(topic, message)).get();
        return 1;
    }

    @Override
    public int sendSync(Collection<String> messages) throws Exception {
        log.debug(PUBLISHING + messages.size() + " messages ");
        for (String message : messages) {
            sendSync(message);
        }
        return messages.size();
    }

    @Override
    public void sendAsync(String partitionKey, String message) throws Exception {
        log.debug("Publishing message on partitionKey " + partitionKey + ": " + message);
        producer.send(new ProducerRecord<String, String>(topic, partitionKey, message));

    }

    @Override
    public void sendAsync(String partitionKey, Collection<String> messages) throws Exception {
        log.debug(PUBLISHING + messages.size() + " messages on partitionKey " + partitionKey);
        for (String message : messages) {
            sendAsync(partitionKey, message);
        }

    }

    @Override
    public void sendAsync(String message) throws Exception {
        log.debug("Publishing message : " + message);
        producer.send(new ProducerRecord<String, String>(topic, message));

    }

    @Override
    public void sendAsync(Collection<String> messages) throws Exception {
        log.debug(PUBLISHING + messages.size() + " messages ");
        for (String message : messages) {
            sendAsync(message);
        }

    }

}
