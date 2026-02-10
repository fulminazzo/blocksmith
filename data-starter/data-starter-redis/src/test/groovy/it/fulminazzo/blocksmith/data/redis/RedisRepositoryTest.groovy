package it.fulminazzo.blocksmith.data.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.Mappers
import org.jetbrains.annotations.NotNull

class RedisRepositoryTest extends RepositoryTest<RedisRepository<User, Long>> {
    private static final Mapper mapper = Mappers.JSON

    private RedisClient client
    private StatefulRedisConnection<String, String> connection

    void setup() {
        client = RedisClient.create('redis://localhost')
        connection = client.connect()

        setupRepository()
    }

    void cleanup() {
        connection.close()
        client.shutdown()
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
