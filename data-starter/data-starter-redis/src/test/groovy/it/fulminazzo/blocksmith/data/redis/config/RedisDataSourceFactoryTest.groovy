package it.fulminazzo.blocksmith.data.redis.config

import redis.embedded.RedisServer
import spock.lang.Specification

class RedisDataSourceFactoryTest extends Specification {
    private static final int serverPort = 16381

    private static RedisServer server

    void setupSpec() {
        server = new RedisServer(serverPort)
        server.start()
    }

    void cleanupSpec() {
        server?.stop()
    }

    def 'test build with #database'() {
        given:
        def config = RedisDataSourceConfig.builder()
                .host('0.0.0.0')
                .port(serverPort)
                .clientName('config-test')
                .ssl(false)
                .database(database)
                .build()

        when:
        def dataSource = new RedisDataSourceFactory().build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()

        where:
        database << [null, 0]
    }

}
