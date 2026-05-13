package it.fulminazzo.blocksmith.data.redis

import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification

import java.time.Duration

class RedisDataSourceTest extends Specification implements RedisIntegrationTest {

    def 'test datasource life cycle'() {
        given:
        def dataSource = RedisDataSource.builder()
                .uri(b -> b.withHost(serverHost).withPort(serverPort))
                .clientOptions(c -> c.autoReconnect(false))
                .socketOptions(s -> s.keepAlive(true))
                .mapper(MapperFormat.JSON.newMapper())
                .build()

        when:
        def repository = dataSource.newRepository(
                User,
                new RedisRepositorySettings()
                        .withDatabaseName('database')
                        .withCollectionName('users')
                        .withTtl(Duration.ofSeconds(1))
        )

        then:
        repository != null

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

}
