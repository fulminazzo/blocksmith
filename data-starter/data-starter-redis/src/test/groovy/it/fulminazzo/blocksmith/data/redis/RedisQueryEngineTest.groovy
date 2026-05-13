package it.fulminazzo.blocksmith.data.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Shared
import spock.lang.Specification

class RedisQueryEngineTest extends Specification implements RedisIntegrationTest {
    private static final Mapper MAPPER = MapperFormat.JSON.newMapper()

    @Shared
    private RedisClient client

    @Shared
    private StatefulRedisConnection<String, String> connection

    @Shared
    private RedisQueryEngine<User, Long> engine

    void setupSpec() {
        client = RedisClient.create("redis://$serverHost:$serverPort")
        connection = client.connect()

        connection.async().mset(
                [Users.SAVED1, Users.SAVED2].collectEntries {
                    [("database:users:$it.id".toString()) : MAPPER.serialize(it)]
                }
        ).get()

        engine = new RedisQueryEngine<>(
                connection,
                EntityMapper.create(User),
                MAPPER,
                'database',
                'users'
        )
    }

    void cleanupSpec() {
        connection?.close()
        client?.shutdown()
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
