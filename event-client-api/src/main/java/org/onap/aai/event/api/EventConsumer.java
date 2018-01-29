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
package org.onap.aai.event.api;

public interface EventConsumer {

    /**
     * Fetches any relevant messages currently on the Event Bus, according to the configuration and manages the offsets
     * for the client.
     *
     * @return List of messages fetched from the Event Bus.
     * @throws Exception
     */
    public Iterable<String> consumeAndCommit() throws Exception;


    /**
     * Fetches any relevant messages currently on the Event Bus, according to the configuration and expects the client
     * to explicitly call the {@link #commitOffsets()} in order to commit the offsets
     *
     * @return List of messages fetched from the Event Bus.
     * @throws Exception
     */
    public Iterable<String> consume() throws Exception;

    /**
     * Fetches any relevant messages currently on the Event Bus with their offsets, according to the configuration and
     * expects the client to explicitly call the {@link #commitOffsets()} in order to commit the offsets
     *
     * @throws Exception
     */
    public Iterable<MessageWithOffset> consumeWithOffsets() throws Exception;

    /**
     * Commits the offsets of the previously consumed messages from the Event Bus
     *
     * @throws Exception if the offsets could not be committed
     */
    public void commitOffsets() throws Exception;

    /**
     * Commits the offsets of messages consumed up to offset
     *
     * @throws Exception if the offsets could not be committed
     */
    public void commitOffsets(long offset) throws Exception;
}
