package it.fulminazzo.blocksmith.data.redis

import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.mapper.Mappers
import redis.embedded.RedisServer
import spock.lang.Specification

class RedisDataSourceTest extends Specification {
    private static final int serverPort = 16378

    private RedisServer server

    void setup() {
        server = new RedisServer(serverPort)
        server.start()
    }

    void cleanup() {
        server?.stop()
    }

    def 'test datasource life cycle'() {
        given:
        def dataSource = RedisDataSource.builder()
                .uri(b ->
                        b.withHost('localhost')
                                .withPort(serverPort)
                )
                .clientOptions(c -> c.autoReconnect(false))
                .socketOptions(s -> s.keepAlive(true))
                .mapper(Mappers.JSON)
                .build()

        when:
        def repository = dataSource.newRepository(User)

        then:
        repository != null

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

}
