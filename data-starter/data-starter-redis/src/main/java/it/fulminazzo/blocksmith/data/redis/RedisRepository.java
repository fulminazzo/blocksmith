package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A basic implementation of {@link Repository} for Redis databases.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (will be used as file names)
 */
@RequiredArgsConstructor
public class RedisRepository<T, ID> {
    private final @NotNull StatefulRedisConnection<String, String> connection;
    private final @NotNull Function<T, ID> idMapper;
    private final @NotNull Class<T> dataType;

    /**
     * Executes a general query and returns the result.
     *
     * @param <R>           the type of the result
     * @param queryFunction the query
     * @return the result
     */
    protected <R> @NotNull CompletableFuture<R> query(
            final @NotNull Function<RedisAsyncCommands<String, String>, RedisFuture<R>> queryFunction
    ) {
        return queryFunction.apply(connection.async()).toCompletableFuture();
    }

}
