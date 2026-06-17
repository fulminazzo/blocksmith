package it.fulminazzo.blocksmith.broker.redis

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.MessageBroker
import it.fulminazzo.blocksmith.broker.MessageBrokerBuilder
import it.fulminazzo.blocksmith.broker.MessageBrokerIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat

@Slf4j
class RedisMessageBrokerIntegrationTest extends MessageBrokerIntegrationTest<RedisMessageChannelSettings> implements RedisIntegrationTest {

    void setup() {
        setupSingle()
    }

    void cleanup() {
        cleanupSingle()
    }

    @Override
    protected MessageBrokerBuilder<MessageBroker<RedisMessageChannelSettings>> newMessageBrokerBuilder() {
        return RedisMessageBroker.builder()
                .uri(b -> b.withHost(serverHost).withPort(serverPort))
                .clientOptions(c -> c.autoReconnect(false))
                .socketOptions(s -> s.keepAlive(true))
                .mapper(MapperFormat.JSON.newMapper())
    }

    @Override
    protected MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new RedisChannelIntegrationTestHelper(channelName, log)
    }

    @Override
    protected RedisMessageChannelSettings getSettings() {
        return new RedisMessageChannelSettings()
    }

}
