package it.fulminazzo.blocksmith.data.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import org.jetbrains.annotations.NotNull

import java.time.Duration

class RedisRepositoryTest extends RepositoryTest<RedisRepository<User, Long>> implements RedisIntegrationTest {
    private static final Mapper mapper = MapperFormat.JSON.newMapper()

    private static RedisClient client
    private static StatefulRedisConnection<String, String> connection

    void setupSpec() {
        client = RedisClient.create("redis://$serverHost:$serverPort")
        connection = client.connect()
    }

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        connection?.close()
        client?.shutdown()
    }

    def 'test that save respects expiration time'() {
        given:
        def expected = Users.NEW1

        and:
        repository.ttl(Duration.ofSeconds(1))

        when:
        def actual = repository.save(expected).get()

        then:
        actual == expected

        when:
        def first = repository.findById(expected.id).get()

        then:
        first.isPresent()
        first.get() == expected

        when:
        Thread.sleep(1001)

        and:
        def second = repository.findById(expected.id).get()

        then:
        !second.isPresent()
    }

    def 'test that saveAll respects expiration time'() {
        given:
        def expected = [Users.NEW1, Users.NEW2]

        and:
        repository.ttl(Duration.ofSeconds(1))

        when:
        def actual = repository.saveAll(expected).get()

        then:
        actual == expected

        when:
        def first = repository.findAllById(expected.collect { it.id }).get()

        then:
        first == expected

        when:
        Thread.sleep(1001)

        and:
        def second = repository.findAllById(expected.collect { it.id }).get()

        then:
        second.isEmpty()
    }

    @Override
    RedisRepository<User, Long> initializeRepository() {
        return new RedisRepository<>(
                new RedisQueryEngine<>(
                        connection,
                        EntityMapper.create(User),
                        mapper,
                        'database',
                        'users'
                ),
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return connection.sync().get("database:users:$id".toString()) != null
    }

    @Override
    void insert(final @NotNull User entity) {
        connection.sync().set("database:users:$entity.id".toString(), mapper.serialize(entity))
    }

    @Override
    void remove(final @NotNull Long id) {
        connection.sync().del("database:users:$id".toString())
    }

}
