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

    private static RedisServer server
    private static RedisClient client
    private static StatefulRedisConnection<String, String> connection

    private static RedisQueryEngine<User, Long> engine

    void setupSpec() {
        server = new RedisServer(serverPort)
        server.start()

        client = RedisClient.create("redis://localhost:$serverPort")
        connection = client.connect()

        connection.async().mset(
                [Users.SAVED1, Users.SAVED2].collectEntries {
                    [("database:users:$it.id".toString()): mapper.serialize(it)]
                }
        ).get()

        engine = new RedisQueryEngine<>(
                connection,
                EntityMapper.create(User),
                mapper,
                'database',
                'users'
        )
    }

    void cleanupSpec() {
        connection?.close()
        client?.shutdown()
        server?.stop()
    }

    def 'test that getValues returns #expected'() {
        given:
        def keys = expected.collect { "database:users:$it.id".toString() }

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
        def expected = [Users.SAVED1, Users.SAVED2].collect { "database:users:$it.id" }

        when:
        def actual = engine.allKeys.get()

        then:
        actual.sort() == expected.sort()
    }

}
