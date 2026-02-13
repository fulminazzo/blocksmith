package it.fulminazzo.blocksmith.data.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.Mappers
import org.jetbrains.annotations.NotNull
import redis.embedded.RedisServer

class RedisRepositoryTest extends RepositoryTest<RedisRepository<User, Long>> {
    private static final Mapper mapper = Mappers.JSON
    private static final int serverPort = 16379

    private static RedisServer server
    private static RedisClient client
    private static StatefulRedisConnection<String, String> connection

    void setupSpec() {
        server = new RedisServer(serverPort)
        server.start()

        client = RedisClient.create("redis://localhost:$serverPort")
        connection = client.connect()
    }

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        if (connection != null) connection.close()
        if (client != null) client.shutdown()
        if (server != null) server.stop()
    }

    @Override
    RedisRepository<User, Long> initializeRepository() {
        return new RedisRepository<>(
                new RedisQueryEngine<>(
                        connection,
                        EntityMapper.create(User),
                        mapper
                ),
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return connection.sync().get(id.toString()) != null
    }

    @Override
    void insert(final @NotNull User entity) {
        connection.sync().set(entity.id.toString(), mapper.serialize(entity))
    }

    @Override
    void remove(final @NotNull Long id) {
        connection.sync().del(id.toString())
    }

}
