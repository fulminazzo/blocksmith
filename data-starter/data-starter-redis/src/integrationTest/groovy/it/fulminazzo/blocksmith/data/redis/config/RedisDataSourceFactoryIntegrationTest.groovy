package it.fulminazzo.blocksmith.data.redis.config

import it.fulminazzo.blocksmith.data.redis.RedisIntegrationTest
import spock.lang.Specification

class RedisDataSourceFactoryIntegrationTest extends Specification implements RedisIntegrationTest {

    def 'test build with #database'() {
        given:
        def config = RedisDataSourceConfig.builder()
                .host(serverHost)
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
