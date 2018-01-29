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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestKafkaEventConsumer {

    @Mock
    public KafkaConsumer<String, String> mockKafkaConsumer;

    @Before
    public void init() throws Exception {
        KafkaEventConsumer.setConsumerFactory(props -> mockKafkaConsumer);
    }

    @Test
    public void testConstructor() {
        new KafkaEventConsumer("", "", "");
    }

    @Test
    public void consumeZeroRecords() throws Exception {
        Mockito.when(mockKafkaConsumer.poll(Mockito.anyLong())).thenReturn(ConsumerRecords.empty());
        KafkaEventConsumer consumer = new KafkaEventConsumer("", "", "");
        consumer.consume();
        consumer.consumeWithOffsets();
        consumer.consumeAndCommit();
        consumer.close();
    }

    @Test
    public void consumeMultipleRecords() throws Exception {
        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new HashMap<>();
        records.put(new TopicPartition(null, 0),
                Arrays.asList(new ConsumerRecord<String, String>("topic", 0, 0, "key", "value")));
        Mockito.when(mockKafkaConsumer.poll(Mockito.anyLong())).thenReturn(new ConsumerRecords<>(records));
        KafkaEventConsumer consumer = new KafkaEventConsumer("", "", "");
        consumer.consume();
        consumer.consumeWithOffsets();
        consumer.consumeAndCommit();
        consumer.close();
    }

    @Test
    public void commitOffsets() throws Exception {
        List<TopicPartition> partitionsList = Arrays.asList(new TopicPartition(null, 0));
        Set<TopicPartition> partitionsSet = Collections.unmodifiableSet(new HashSet<TopicPartition>(partitionsList));
        Mockito.when(mockKafkaConsumer.assignment()).thenReturn(partitionsSet);
        KafkaEventConsumer consumer = new KafkaEventConsumer("", "", "");
        consumer.commitOffsets();
        consumer.commitOffsets(0L);
        consumer.close();
    }

}
