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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventConsumer;
import org.onap.aai.event.api.MessageWithOffset;

/**
 * Event Bus Client consumer API for Kafka Implementation .Its a wrapper around KafkaConsumer which is NOT thread safe.
 * The KafkaConsumer maintains TCP connections to the necessary brokers to fetch data. Failure to close the consumer
 * after use will leak these connections Ref : https://kafka.apache.org/0102/javadoc/org/apache/kafka/clients/consumer/
 * KafkaConsumer.html
 *
 */
public class KafkaEventConsumer implements EventConsumer {

    private static Logger log = LoggerFactory.getInstance().getLogger(KafkaEventConsumer.class);

    @FunctionalInterface
    public interface KafkaConsumerFactory {
        public KafkaConsumer<String, String> createConsumer(Properties props);
    }

    private static KafkaConsumerFactory consumerFactory = KafkaConsumer::new;

    private final KafkaConsumer<String, String> consumer;

    /**
     *
     * @param hosts - A list of host/port pairs to use for establishing the initial connection to the Kafka cluster. The
     *        client will make use of all servers irrespective of which servers are specified here for
     *        bootstrapping—this list only impacts the initial hosts used to discover the full set of servers. This list
     *        should be in the form host1:port1,host2:port2,....
     * @param topic - Topic to consume the messages from
     * @param groupId - A unique string that identifies the consumer group this consumer belongs to
     */
    public KafkaEventConsumer(String hosts, String topic, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Set this property, if auto commit should happen.
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumer = consumerFactory.createConsumer(props);
        consumer.subscribe(Arrays.asList(topic));
    }

    /**
     * Replace the consumer factory (intended to be used for testing purposes only).
     * 
     * @param consumerFactory
     */
    static void setConsumerFactory(KafkaConsumerFactory consumerFactory) {
        KafkaEventConsumer.consumerFactory = consumerFactory;
    }

    @Override
    public void close() {
        consumer.close();
    }

    /**
    *
    */
    @Override
    public Iterable<String> consume() throws Exception {
        log.debug("Querying Kafka for messages");
        ConsumerRecords<String, String> records = consumer.poll(0);
        List<String> list = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            list.add(record.value());
        }
        return list;

    }

    @Override
    public Iterable<MessageWithOffset> consumeWithOffsets() throws Exception {
        log.debug("Querying Kafka for messages");
        ConsumerRecords<String, String> records = consumer.poll(0);
        List<MessageWithOffset> list = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            list.add(new MessageWithOffset(record.offset(), record.value()));
        }
        return list;
    }

    @Override
    public Iterable<String> consumeAndCommit() throws Exception {
        Iterable<String> result = consume();
        consumer.commitSync();
        return result;
    }

    @Override
    public void commitOffsets() throws Exception {
        consumer.commitSync();
    }

    @Override
    public void commitOffsets(long offset) throws Exception {
        Map<TopicPartition, OffsetAndMetadata> offsetsMap = new HashMap<>();
        offsetsMap.put(consumer.assignment().iterator().next(), new OffsetAndMetadata(offset));
        consumer.commitSync(offsetsMap);
    }

}
