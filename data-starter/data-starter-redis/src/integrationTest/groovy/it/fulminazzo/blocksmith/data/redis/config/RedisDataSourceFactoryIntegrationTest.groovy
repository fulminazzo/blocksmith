package it.fulminazzo.blocksmith.data.redis.config

import it.fulminazzo.blocksmith.ProjectInfo
import it.fulminazzo.blocksmith.data.redis.RedisIntegrationTest
import spock.lang.Specification

class RedisDataSourceFactoryIntegrationTest extends Specification implements RedisIntegrationTest {

    def 'test build with clientName=#clientName and database#database'() {
        given:
        def config = new RedisDataSourceConfig()
                .setHost(serverHost)
                .setPort(serverPort)
                .setClientName(clientName)
                .setSsl(false)
                .setDatabase(database)

        when:
        def dataSource = new RedisDataSourceFactory().build(config)

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
        def config = new RedisDataSourceConfig().setHost(serverHost).setPort(null)

        when:
        new RedisDataSourceFactory().build(config)

        then:
        thrown(io.lettuce.core.RedisConnectionException)
    }

}
