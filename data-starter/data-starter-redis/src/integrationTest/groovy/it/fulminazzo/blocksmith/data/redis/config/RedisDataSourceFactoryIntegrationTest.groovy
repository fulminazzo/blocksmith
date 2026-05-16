package it.fulminazzo.blocksmith.data.redis.config

import it.fulminazzo.blocksmith.ProjectInfo
import it.fulminazzo.blocksmith.data.redis.RedisIntegrationTest
import spock.lang.Specification

class RedisDataSourceFactoryIntegrationTest extends Specification implements RedisIntegrationTest {

    def 'test build with #database'() {
        given:
        def config = new RedisDataSourceConfig()
                .withHost(serverHost)
                .withPort(serverPort)
                .withClientName(ProjectInfo.PROJECT_NAME)
                .withSsl(false)
                .withDatabase(database)

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
