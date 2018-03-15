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
package org.onap.aai.event.api;

import java.util.Collection;

public interface EventPublisher {

    /**
     * Publishes a message using the supplied partition key, using the parameters from the constructor.
     *
     * @param partitionKey The partition to publish the message on.
     * @param message The String message to publish.
     * @return The number of messages successfully sent
     * @throws Exception
     */
    public int sendSync(String partitionKey, String message) throws Exception;

    /**
     * Publishes a message using the supplied partition key, using the parameters from the constructor.
     *
     * @param partitionKey The partition to publish the messages on.
     * @param messages A Collection of messages to publish.
     * @return The number of messages successfully sent
     * @throws Exception
     */
    public int sendSync(String partitionKey, Collection<String> messages) throws Exception;

    /**
     * Publishes a message using the parameters from the constructor.
     *
     * @param message The String message to publish.
     * @return The number of messages successfully sent
     * @throws Exception
     */
    public int sendSync(String message) throws Exception;

    /**
     * Publishes a message using the parameters from the constructor.
     *
     * @param messages A Collection of messages to publish.
     * @return The number of messages successfully sent
     * @throws Exception
     */
    public int sendSync(Collection<String> messages) throws Exception;

    /**
     * Publishes a message using the supplied partition key, using the parameters from the constructor. The Async method
     * returns immediately without caring if the message was properly published or not.
     *
     * @param partitionKey The partition to publish the message on.
     * @param message The String message to publish.
     * @throws Exception
     */
    public void sendAsync(String partitionKey, String message) throws Exception;

    /**
     * Publishes a message using the supplied partition key, using the parameters from the constructor. The Async method
     * returns immediately without caring if the message was properly published or not.
     *
     * @param partitionKey The partition to publish the messages on.
     * @param messages A Collection of messages to publish.
     * @throws Exception
     */
    public void sendAsync(String partitionKey, Collection<String> messages) throws Exception;

    /**
     * Publishes a message using the parameters from the constructor. The Async method returns immediately without
     * caring if the message was properly published or not.
     *
     * @param message The String message to publish.
     * @throws Exception
     */
    public void sendAsync(String message) throws Exception;

    /**
     * Publishes a message using the parameters from the constructor. The Async method returns immediately without
     * caring if the message was properly published or not.
     *
     * @param messages A Collection of messages to publish.
     * @throws Exception
     */
    public void sendAsync(Collection<String> messages) throws Exception;

    /**
     * Closes the publisher.
     */
    public void close() throws Exception;


}
