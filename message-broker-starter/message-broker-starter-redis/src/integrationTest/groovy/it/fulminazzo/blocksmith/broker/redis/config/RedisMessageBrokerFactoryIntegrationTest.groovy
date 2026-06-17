package it.fulminazzo.blocksmith.broker.redis.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactoryIntegrationTest
import it.fulminazzo.blocksmith.broker.redis.RedisIntegrationTest

class RedisMessageBrokerFactoryIntegrationTest extends MessageBrokerFactoryIntegrationTest implements RedisIntegrationTest {

    @Override
    protected MessageBrokerFactory getFactory() {
        return new RedisMessageBrokerFactory()
    }

    @Override
    protected MessageBrokerConfig getConfig() {
        return new RedisMessageBrokerConfig()
                .setHost(RedisIntegrationTest.serverHost)
                .setPort(RedisIntegrationTest.serverPort)
    }

}
