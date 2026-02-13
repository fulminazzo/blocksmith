package it.fulminazzo.blocksmith.data.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.Mappers
import redis.embedded.RedisServer
import spock.lang.Specification

class RedisQueryEngineTest extends Specification {
    private static final Mapper mapper = Mappers.JSON
    private static final int serverPort = 16380

    private RedisServer server
    private RedisClient client
    private StatefulRedisConnection<String, String> connection

    private RedisQueryEngine<User, Long> engine

    void setup() {
        server = new RedisServer(serverPort)
        server.start()

        client = RedisClient.create("redis://localhost:$serverPort")
        connection = client.connect()

        connection.async().mset(
                [Users.SAVED1, Users.SAVED2].collectEntries {
                    [(it.id.toString()): mapper.serialize(it)]
                }
        )

        engine = new RedisQueryEngine<>(
                connection,
                EntityMapper.create(User),
                mapper
        )
    }

    void cleanup() {
        if (connection != null) connection.close()
        if (client != null) client.shutdown()
        if (server != null) server.stop()
    }

    def 'test that getValues returns #expected'() {
        given:
        def keys = expected.collect { it.id.toString() }

        when:
        def actual = engine.getValues(keys).get()

        then:
        actual.sort() == expected.sort()

        where:
        expected << [
                [Users.SAVED1],
                [Users.SAVED2],
                [Users.SAVED1, Users.SAVED2]
        ]
    }

    def 'test that getAllKeys returns all keys'() {
        given:
        def expected = [Users.SAVED1, Users.SAVED2].collect { it.id.toString() }

        when:
        def actual = engine.allKeys.get()

        then:
        actual.sort() == expected.sort()
    }

}
