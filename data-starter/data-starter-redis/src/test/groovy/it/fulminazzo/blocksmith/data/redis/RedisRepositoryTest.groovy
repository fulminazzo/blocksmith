package it.fulminazzo.blocksmith.data.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.Mappers
import org.jetbrains.annotations.NotNull
import redis.embedded.RedisServer

class RedisRepositoryTest extends RepositoryTest<RedisRepository<User, Long>> {
    private static final Mapper mapper = Mappers.JSON
    private static final int serverPort = 16379

    private RedisServer server
    private RedisClient client
    private StatefulRedisConnection<String, String> connection

    void setup() {
        server = new RedisServer(serverPort)
        server.start()

        client = RedisClient.create("redis://localhost:$serverPort")
        connection = client.connect()

        setupRepository()
    }

    void cleanup() {
        connection.close()
        client.shutdown()
        server.stop()
    }

    def 'test that getValues returns #expected'() {
        given:
        def keys = expected.collect { it.id.toString() }

        when:
        def actual = repository.getValues(keys).get()

        then:
        actual.sort() == expected.sort()

        where:
        expected << [
                [FIRST],
                [SECOND],
                [FIRST, SECOND]
        ]
    }

    def 'test that getAllKeys returns all keys'() {
        given:
        def expected = [FIRST, SECOND].collect { it.id.toString() }

        when:
        def actual = repository.allKeys.get()

        then:
        actual.sort() == expected.sort()
    }

    @Override
    RedisRepository<User, Long> initializeRepository() {
        return new RedisRepository<>(
                connection,
                User::getId,
                User,
                mapper
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return connection.sync().get(id.toString()) != null
    }

    @Override
    void insert(final @NotNull User data) {
        connection.sync().set(data.id.toString(), mapper.serialize(data))
    }

}
