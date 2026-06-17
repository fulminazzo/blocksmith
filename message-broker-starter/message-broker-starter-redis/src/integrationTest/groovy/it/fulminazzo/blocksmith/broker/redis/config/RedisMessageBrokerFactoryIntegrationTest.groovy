package it.fulminazzo.blocksmith.broker.redis.config

import it.fulminazzo.blocksmith.ProjectInfo
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactoryIntegrationTest
import it.fulminazzo.blocksmith.broker.redis.RedisIntegrationTest

class RedisMessageBrokerFactoryIntegrationTest extends MessageBrokerFactoryIntegrationTest implements RedisIntegrationTest {

    def 'test build with clientName=#clientName and database#database'() {
        given:
        def config = new RedisMessageBrokerConfig()
                .setHost(RedisIntegrationTest.serverHost)
                .setPort(RedisIntegrationTest.serverPort)
                .setClientName(clientName)
                .setSsl(false)
                .setDatabase(database)

        when:
        def dataSource = new RedisMessageBrokerFactory().build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()

        where:
        database | clientName
        null     | null
        0        | null
        null     | ProjectInfo.PROJECT_NAME
        0        | ProjectInfo.PROJECT_NAME
    }

    /**
     * These tests are purely for coverage purposes.
     * Although, they ensure that if the port has not been specified, the default one is used.
     */
    def 'test build with unspecified port'() {
        given:
        def config = new RedisMessageBrokerConfig()
                .setHost(RedisIntegrationTest.serverHost)
                .setPort(null)

        when:
        new RedisMessageBrokerFactory().build(config)

        then:
        thrown(io.lettuce.core.RedisConnectionException)
    }

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
