DMaaP Client based API
======================

ONAP Gerrit repo: \ https://gerrit.onap.org/r/#/admin/projects/aai/event-client

This is a thin API library based on the DMaaP Client, and exposes
methods that can be used to publish and consume JSON messages to/from a
DMaaP topic.

Importing the library
=====================

To add the Event Bus Client Library to your project simply include the
following dependencies in your project's pom.xml. Note that
the httpclient dependency may be required in your project pom if you are
also depending on AJSC artifacts, as they use an earlier version of
the httpclient which may overwrite the later version required by the
library.

<dependency>

<groupId>org.onap.aai.event-client</groupId>

<artifactId>event-client-dmaap</artifactId>

<version>1.2.0-SNAPSHOT</version>

</dependency>

<!-- This dependency is required if you also have AJSC dependencies -->

<dependency>

<groupId>org.apache.httpcomponents</groupId>

<artifactId>httpclient</artifactId>

<version>4.5.2</version>

</dependency>

<!-- This dependency is required if you also have AJSC dependencies -->

**Note** - In order to retrieve all of the client's transitive
dependencies you may need to configure your maven settings.xml with the
ONAP repositories.

Publishing
==========

Here is an example of how to use the client to publish a message to the
DMaaP topic.

DMaaPEventPublisher eventPublisher = new DMaaPEventPublisher(host,
topic, username, password);

try {

int result = eventPublisher.sendSync(partition, messages);

// result should equal number of messages supplied to sendSync

} catch (...) {

} finally {

List<String> unsentMessages = eventPublisher.closeWithUnsent()

// Handle unsent messages

}

**host** - The host URL to use to connect to the DMaaP topic, in the
form of <host:port>.

**topic** - The topic name to publish to. This must be an existing DMaaP
topic.

**username** - The MechID of the calling application.

**password** - The password for the MechID.

**partition** - The partition to publish the message to.

**message** - A List of JSON String messages to publish as a batch.

Consuming
---------

Here is an example of how to use the client to consume messages from the
UEB.

try {

DMaaPEventConsumer consumer = new DMaaPEventConsumer(host, topic,
username, password, consumerGroup, consumerId)

Iterable<String> messages = consumer.consume();

for(String message : messages) {

// process message

}

} catch(...) {}

**host** - The host URL to use to connect to the DMaaP topic, in the
form of <host:port>.

**topic** - The topic name to consume from. This must be an existing
topic.

**username** - The MechID of the calling application.

**password** - The password for the MechID.

**consumerGroup** - The name of the consumer group this consumer is part
of.

**consumerId** - The unique id of this consumer in its group.

Creating Topics and Authentication
==================================

For information about creating and managing DMaaP topics, refer to the
`DMaaP DBCAPI <https://gerrit.onap.org/r/#/admin/projects/dmaap/dbcapi>`__
project documentation.
