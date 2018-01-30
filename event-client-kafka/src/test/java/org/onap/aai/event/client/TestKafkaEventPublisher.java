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
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestKafkaEventPublisher {

    @Mock
    public KafkaProducer<String, String> mockKafkaProducer;

    @Mock
    private Future<RecordMetadata> mockedFuture;

    @Before
    public void init() throws Exception {
        KafkaEventPublisher.setProducerFactory(props -> mockKafkaProducer);
    }

    @Test
    public void testConstructors() {
        new KafkaEventPublisher("hosts", "topic");
        new KafkaEventPublisher("hosts", "topic", 0, 0, 0);
    }

    @Test
    public void publishSynchronous() throws Exception {
        Mockito.when(mockKafkaProducer.send(Mockito.any())).thenReturn(mockedFuture);
        KafkaEventPublisher publisher = new KafkaEventPublisher("hosts", "topic");
        publisher.sendSync("");
        publisher.sendSync(Arrays.asList(""));
        publisher.sendSync("key", "");
        publisher.sendSync("key", Arrays.asList(""));
        publisher.close();
    }

    @Test
    public void publishAsynchronous() throws Exception {
        KafkaEventPublisher publisher = new KafkaEventPublisher("hosts", "topic");
        publisher.sendAsync("");
        publisher.sendAsync(Arrays.asList(""));
        publisher.sendAsync("key", "");
        publisher.sendAsync("key", Arrays.asList(""));
        publisher.close();
    }
}
