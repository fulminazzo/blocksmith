package it.fulminazzo.blocksmith.broker.rabbitmq

import org.testcontainers.containers.RabbitMQContainer

interface RabbitMQIntegrationTest {
    RabbitMQContainer RABBIT_MQ_SERVER = new RabbitMQContainer('rabbitmq:4.3.0-management-alpine')
            .withReuse(true)

    default String getServerHost() {
        return container.host
    }

    default int getServerPort() {
        return container.amqpPort
    }

    default RabbitMQContainer getContainer() {
        if (!RABBIT_MQ_SERVER.created) RABBIT_MQ_SERVER.start()
        return RABBIT_MQ_SERVER
    }

}
