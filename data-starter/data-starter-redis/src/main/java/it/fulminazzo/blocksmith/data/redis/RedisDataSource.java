package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class RedisDataSource implements Closeable {
    private final @NotNull RedisClient redisClient;
    private final @NotNull StatefulRedisConnection<String, String> connection;

    private final @NotNull Mapper mapper;

    /**
     * Instantiates a new Redis data source.
     *
     * @param redisClient the redis client
     * @param mapper      the mapper
     */
    RedisDataSource(final @NotNull RedisClient redisClient,
                    final @NotNull Mapper mapper) {
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
        this.mapper = mapper;
    }

    /**
     * Creates a new repository.
     *
     * @param <T>      the type of the data
     * @param <ID>     the type of the id
     * @param idMapper the function to get the id from a data object
     * @param dataType the data type
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Function<T, ID> idMapper,
            final @NotNull Class<T> dataType
    ) {
        return new RedisRepository<>(
                connection,
                idMapper,
                dataType,
                mapper
        );
    }

    /**
     * Creates a new custom repository.
     *
     * @param <R>                the type of the repository
     * @param <T>                the type of the data
     * @param <ID>               the type of the id
     * @param repositorySupplier the repository creation function
     * @return the repository
     */
    public <R extends RedisRepository<T, ID>, T, ID> @NotNull R newRepository(
            final @NotNull BiFunction<StatefulRedisConnection<String, String>, Mapper, R> repositorySupplier
    ) {
        return repositorySupplier.apply(connection, mapper);
    }

    @Override
    public void close() {
        connection.close();
        redisClient.shutdown();
    }

}
