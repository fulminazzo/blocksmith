package it.fulminazzo.blocksmith.broker.kafka

import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.Messages

import java.util.concurrent.Executors

class KafkaMessageChannelIntegrationTest extends MessageChannelIntegrationTest {

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
        return new KafkaMessageChannel(
                new KafkaMessageQueryEngine(
                        Executors.newCachedThreadPool(),
                        KafkaChannelIntegrationTestHelper.properties,
                        CHANNEL_NAME,
                        null,
                        KafkaChannelIntegrationTestHelper.ASSIGNMENT_WAIT_MILLIS,
                        KafkaChannelIntegrationTestHelper.CONSUMER_POLL_MILLIS_INTERVAL
                ),
                MessageChannelIntegrationTestHelper.MAPPER
        )
    }

    @Override
    MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new KafkaChannelIntegrationTestHelper(channelName, logger)
    }

}
