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

import com.att.nsa.mr.client.impl.MRConsumerImpl;
import java.net.MalformedURLException;
import javax.naming.OperationNotSupportedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestDMaaPEventConsumer {

    @Mock
    public MRConsumerImpl mockDMaaPConsumer;

    @Before
    public void init() throws Exception {
        DMaaPEventConsumer
                .setConsumerFactory((hosts, topic, g, id, timeout, limit, f, user, pass) -> mockDMaaPConsumer);
    }

    @Test
    public void testConstructors() throws MalformedURLException {
        createConsumerWithDefaults();
        new DMaaPEventConsumer("", "", "", "", "", "", 0, 0, "");
    }

    @Test
    public void consumeZeroRecords() throws Exception {
        DMaaPEventConsumer consumer = createConsumerWithDefaults();
        consumer.consume();
        consumer.consumeAndCommit();
    }

    @Test(expected = OperationNotSupportedException.class)
    public void consumeWithOffsets() throws Exception {
        DMaaPEventConsumer consumer = createConsumerWithDefaults();
        consumer.consumeWithOffsets();
    }

    @Test(expected = OperationNotSupportedException.class)
    public void commitOffsets() throws Exception {
        DMaaPEventConsumer consumer = createConsumerWithDefaults();
        consumer.commitOffsets();
    }

    @Test(expected = OperationNotSupportedException.class)
    public void commitOffsetsLong() throws Exception {
        DMaaPEventConsumer consumer = createConsumerWithDefaults();
        consumer.commitOffsets(0);
    }

    private DMaaPEventConsumer createConsumerWithDefaults() throws MalformedURLException {
        return new DMaaPEventConsumer("", "", "", "", "", "");
    }
}
