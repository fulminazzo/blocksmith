package it.fulminazzo.blocksmith.broker.redis

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.MessageBroker
import it.fulminazzo.blocksmith.broker.MessageBrokerBuilder
import it.fulminazzo.blocksmith.broker.MessageBrokerIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat

@Slf4j
class RedisMessageBrokerIntegrationTest extends MessageBrokerIntegrationTest<RedisMessageChannelSettings> {
    private static final String CHANNEL_NAME = 'redis-message-broker'

    void setup() {
        setupSingle()
    }

    void cleanup() {
        cleanupSingle()
    }

    @Override
    protected MessageBrokerBuilder<MessageBroker<RedisMessageChannelSettings>> newMessageBrokerBuilder() {
        return RedisMessageBroker.builder()
                .uri(b -> b
                        .withHost(RedisChannelIntegrationTestHelper.serverHost)
                        .withPort(RedisChannelIntegrationTestHelper.serverPort)
                )
                .clientOptions(c -> c.autoReconnect(false))
                .socketOptions(s -> s.keepAlive(true))
                .mapper(MapperFormat.JSON.newMapper())
    }

    @Override
    protected MessageChannelIntegrationTestHelper newTestHelper() {
        return new RedisChannelIntegrationTestHelper(CHANNEL_NAME, log)
    }

    @Override
    protected RedisMessageChannelSettings getSettings() {
        return new RedisMessageChannelSettings()
                .withChannelName(CHANNEL_NAME)
                .broadcast()
    }

}
