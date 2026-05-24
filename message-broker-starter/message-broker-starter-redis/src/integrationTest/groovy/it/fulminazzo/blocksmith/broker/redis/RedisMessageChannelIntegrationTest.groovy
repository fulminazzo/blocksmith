package it.fulminazzo.blocksmith.broker.redis

import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTest
import it.fulminazzo.blocksmith.broker.Messages

class RedisMessageChannelIntegrationTest extends MessageChannelIntegrationTest {

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

    def 'test that send from other channel does not return to current'() {
        given:
        channel.queryEngine.pubSubConnection.sync().subscribe('other-channel')

        when:
        helper.connection.sync().publish(
                'other-channel',
                RedisChannelIntegrationTestHelper.serializeMessage(message, UUID.randomUUID())
        )

        then:
        !received(message.id)

        cleanup:
        channel?.queryEngine?.pubSubConnection?.sync()?.unsubscribe('other-channel')

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    @Override
    MessageChannel initializeChannel() {
        RedisChannelIntegrationTestHelper helper = new RedisChannelIntegrationTestHelper(CHANNEL_NAME, logger)
        return new RedisMessageChannel(
                new RedisMessageQueryEngine(
                        CHANNEL_NAME,
                        helper.connection,
                        helper.pubSubConnection
                ),
                MessageChannelIntegrationTestHelper.MAPPER
        )
    }

    @Override
    MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new RedisChannelIntegrationTestHelper(channelName, logger)
    }

}
