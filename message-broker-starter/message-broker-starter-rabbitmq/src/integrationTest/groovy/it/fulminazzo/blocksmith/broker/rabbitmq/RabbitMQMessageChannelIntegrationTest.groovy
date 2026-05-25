package it.fulminazzo.blocksmith.broker.rabbitmq

import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.Messages

import java.util.concurrent.Executors

class RabbitMQMessageChannelIntegrationTest extends MessageChannelIntegrationTest {

    void setup() {
        setupChannel()
    }

    void cleanup() {
        clearData()
    }

    def 'test that sending on server works'() {
        when:
        send(message, UUID.randomUUID())

        and:
        sleep(SLEEP_TIME)

        then:
        received(message.id)

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    @Override
    MessageChannel initializeChannel() {
        RabbitMQChannelIntegrationTestHelper helper = new RabbitMQChannelIntegrationTestHelper(CHANNEL_NAME, logger)
        return new RabbitMQMessageChannel(
                new RabbitMQMessageQueryEngine(
                        Executors.newCachedThreadPool(),
                        CHANNEL_NAME,
                        helper.channel,
                        '',
                        new RabbitMQMessageChannelSettings.QueueSettings()
                                .withQueueName(RabbitMQChannelIntegrationTestHelper.QUEUE_NAME)
                                .durable()
                ),
                MessageChannelIntegrationTestHelper.MAPPER
        )
    }

    @Override
    MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new RabbitMQChannelIntegrationTestHelper(channelName, logger)
    }

}
