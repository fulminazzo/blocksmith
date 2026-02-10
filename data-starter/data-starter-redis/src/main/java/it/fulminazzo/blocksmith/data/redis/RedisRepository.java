package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A basic implementation of {@link Repository} for Redis databases.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (will be used as file names)
 */
public class RedisRepository<T, ID> {
    private final @NotNull RedisAsyncCommands<String, String> context;
    private final @NotNull Function<T, ID> idMapper;
    private final @NotNull Class<T> dataType;

    /**
     * Instantiates a new Redis repository.
     *
     * @param connection the redis connection
     * @param dataType   the type of the data
     * @param idMapper   the function to get the id from data
     */
    protected RedisRepository(final @NotNull StatefulRedisConnection<String, String> connection,
                              final @NotNull Class<T> dataType,
                              final @NotNull Function<T, ID> idMapper) {
        this.context = connection.async();
        this.dataType = dataType;
        this.idMapper = idMapper;
    }

}
