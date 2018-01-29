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

import com.att.nsa.mr.client.MRPublisher.message;
import com.att.nsa.mr.client.impl.MRSimplerBatchPublisher;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Properties;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aai.event.client.DMaaPEventPublisher.MRPublisherFactory;

@RunWith(MockitoJUnitRunner.class)
public class TestDMaaPEventPublisher {

    private static MRPublisherFactory defaultFactory = DMaaPEventPublisher.getPublisherFactory();

    @Mock
    public MRSimplerBatchPublisher mockDmaapPublisher;

    @After
    public void restorePublisherFactory() {
        DMaaPEventPublisher.setPublisherFactory(defaultFactory);
    }

    public void mockPublisherFactory() {
        DMaaPEventPublisher.setPublisherFactory((host, topic, size, age, delay) -> mockDmaapPublisher);
    }

    @Test
    public void testDefaultFactory() throws MalformedURLException {
        createPublisherWithDefaults();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePublisherWithoutHost() throws MalformedURLException {
        new DMaaPEventPublisher("", "topic", "", "");
    }

    @Test
    public void testConstructors() throws MalformedURLException {
        createMockedPublisher();
        new DMaaPEventPublisher("", "", "", "", 0, 0, 0, "");
    }

    @Test
    public void publishSynchronous() throws Exception {
        Mockito.when(mockDmaapPublisher.getProps()).thenReturn(new Properties());
        message message = new message("partition", "message");
        Mockito.when(mockDmaapPublisher.close(Mockito.anyInt(), Mockito.any())).thenReturn(Arrays.asList(message));
        DMaaPEventPublisher publisher = createMockedPublisher();
        publisher.sendSync("");
        publisher.sendSync(Arrays.asList(""));
        publisher.sendSync("key", "");
        publisher.sendSync("key", Arrays.asList(""));
        publisher.close();
        publisher.closeWithUnsent();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void sendAsync() throws Exception {
        DMaaPEventPublisher publisher = createMockedPublisher();
        publisher.sendAsync("");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void sendAsyncMultiple() throws Exception {
        DMaaPEventPublisher publisher = createMockedPublisher();
        publisher.sendAsync(Arrays.asList(""));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void sendAsyncWithPartition() throws Exception {
        DMaaPEventPublisher publisher = createMockedPublisher();
        publisher.sendAsync("partition", "");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void sendAsyncWithPartitionMultiple() throws Exception {
        DMaaPEventPublisher publisher = createMockedPublisher();
        publisher.sendAsync("partition", Arrays.asList(""));
    }

    private DMaaPEventPublisher createPublisherWithDefaults() throws MalformedURLException {
        return new DMaaPEventPublisher("host", "topic", "", "");
    }

    private DMaaPEventPublisher createMockedPublisher() throws MalformedURLException {
        mockPublisherFactory();
        return createPublisherWithDefaults();
    }
}
