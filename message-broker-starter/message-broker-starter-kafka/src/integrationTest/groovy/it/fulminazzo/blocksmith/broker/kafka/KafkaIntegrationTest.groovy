//file:noinspection GroovyAccessibility
package it.fulminazzo.blocksmith.broker.kafka

import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.kafka.KafkaHelper

interface KafkaIntegrationTest {
    static final ConfluentKafkaContainer KAFKA_SERVER = new ConfluentKafkaContainer('confluentinc/cp-kafka:7.4.0')
            .withReuse(true)

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods')
    static String getServerHost() {
        return container.host
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods')
    static int getServerPort() {
        return container.getMappedPort(KafkaHelper.KAFKA_PORT)
    }

    static ConfluentKafkaContainer getContainer() {
        if (!KAFKA_SERVER.created) KAFKA_SERVER.start()
        return KAFKA_SERVER
    }

}
