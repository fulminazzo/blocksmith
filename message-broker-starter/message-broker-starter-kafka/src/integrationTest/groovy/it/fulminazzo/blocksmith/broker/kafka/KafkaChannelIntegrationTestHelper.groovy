//file:noinspection GroovyAccessibility
package it.fulminazzo.blocksmith.broker.kafka

import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger

import java.util.function.Consumer

@SuppressWarnings('CloseWithoutCloseable')
class KafkaChannelIntegrationTestHelper extends MessageChannelIntegrationTestHelper implements KafkaIntegrationTest {
    static final String GROUP_ID = 'integration-tests-group'
    static final String HELPER_GROUP_ID = 'tests-helper-group'

    static final int ASSIGNMENT_WAIT_MILLIS = 60_000
    static final int CONSUMER_POLL_MILLIS_INTERVAL = 125

    private final List<KafkaConsumerHandler<String, String>> consumers = []

    private final KafkaProducer<String, String> producer

    private final Properties consumerProperties

    KafkaChannelIntegrationTestHelper(final String channelName, final Logger logger) {
        super(channelName, logger)
        this.consumerProperties = properties
        this.consumerProperties['group.id'] = HELPER_GROUP_ID
        producer = new KafkaProducer<>(consumerProperties)
    }

    @Override
    void send(final Message message, final UUID conversationId) {
        def payload = new ProducerRecord<>(channelName, null as String, serializeMessage(message, conversationId))
        producer.send(payload)
    }

    @Override
    void close() throws IOException {
        consumers*.close()
        producer?.close()
        super.close()
    }

    @Override
    protected MessageChannelIntegrationTestHelper start(
            final String channelName,
            final Logger logger,
            final Consumer<String> consumer
    ) {
        consumers.add(new KafkaConsumerHandler<String, String>(
                consumerProperties,
                [channelName],
                ASSIGNMENT_WAIT_MILLIS,
                CONSUMER_POLL_MILLIS_INTERVAL
        ) {

            @Override
            protected void handle(final @NotNull String key, final @NotNull String value) {
                consumer.accept(value)
            }

        })
        return this
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static Properties getProperties() {
        final Properties properties = new Properties()
        properties['bootstrap.servers'] = "$KafkaIntegrationTest.host:$KafkaIntegrationTest.port".toString()
        properties['key.serializer'] = StringSerializer.canonicalName
        properties['value.serializer'] = StringSerializer.canonicalName
        properties['key.deserializer'] = StringDeserializer.canonicalName
        properties['value.deserializer'] = StringDeserializer.canonicalName

        properties['group.id'] = GROUP_ID
        properties['auto.offset.reset'] = 'latest'

        return properties
    }

}
