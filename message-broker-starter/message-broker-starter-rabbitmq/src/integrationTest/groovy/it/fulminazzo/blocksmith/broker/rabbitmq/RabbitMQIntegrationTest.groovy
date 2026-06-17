package it.fulminazzo.blocksmith.broker.rabbitmq

import org.testcontainers.containers.RabbitMQContainer

interface RabbitMQIntegrationTest {
    static final RabbitMQContainer RABBIT_MQ_SERVER = new RabbitMQContainer('rabbitmq:4.3.0-management-alpine')
            .withReuse(true)

    static String getServerHost() {
        return container.host
    }

    static int getServerPort() {
        return container.amqpPort
    }

    static RabbitMQContainer getContainer() {
        if (!RABBIT_MQ_SERVER.created) RABBIT_MQ_SERVER.start()
        return RABBIT_MQ_SERVER
    }

}
