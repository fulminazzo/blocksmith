//file:noinspection GroovyAccessibility
package it.fulminazzo.blocksmith.broker.kafka

import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.kafka.KafkaHelper

interface KafkaIntegrationTest {
    ConfluentKafkaContainer KAFKA_SERVER = new ConfluentKafkaContainer('confluentinc/cp-kafka:7.4.0')
            .withReuse(true)

    default String getServerHost() {
        return container.host
    }

    default int getServerPort() {
        return container.getMappedPort(KafkaHelper.KAFKA_PORT)
    }

    static ConfluentKafkaContainer getContainer() {
        if (!KAFKA_SERVER.created) KAFKA_SERVER.start()
        return KAFKA_SERVER
    }

    static String getHost() {
        return container.host
    }

    static int getPort() {
        return container.getMappedPort(KafkaHelper.KAFKA_PORT)
    }

}
