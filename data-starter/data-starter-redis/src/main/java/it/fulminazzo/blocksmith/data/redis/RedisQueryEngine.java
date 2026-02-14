package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import it.fulminazzo.blocksmith.data.QueryEngine;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A query engine with Redis support.
 * <br>
 * Uses the <a href="https://lettuce.io">lettuce</a> library under the hood
 * to leverage the speed and optimizations provided by Netty asynchronous operations.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class RedisQueryEngine<T, ID> implements QueryEngine<T, ID> {
    private final @NotNull StatefulRedisConnection<String, String> connection;
    private final @NotNull EntityMapper<T, ID> entityMapper;
    private final @NotNull Mapper mapper;

    /**
     * Queries the database to get all the values of the corresponding keys.
     *
     * @param keys the keys
     * @return the values
     */
    public @NotNull CompletableFuture<Collection<T>> getValues(final @NotNull Collection<String> keys) {
        return query(async ->
                async.mget(keys.toArray(String[]::new))
        ).thenApply(l -> l.stream()
                .filter(KeyValue::hasValue)
                .map(KeyValue::getValue)
                .map(v -> mapper.deserialize(v, entityMapper.getType()))
                .collect(Collectors.toList())
        );
    }

    /**
     * Gets all the currently stored keys in the database.
     *
     * @return the keys
     */
    public @NotNull CompletableFuture<Collection<String>> getAllKeys() {
        return scanAllKeys(connection.async(), new ArrayList<>(), ScanCursor.INITIAL);
    }

    private @NotNull CompletableFuture<Collection<String>> scanAllKeys(
            final @NotNull RedisAsyncCommands<String, String> async,
            final @NotNull List<String> keys,
            final @Nullable ScanCursor cursor
    ) {
        return (cursor == null ? async.scan() : async.scan(cursor))
                .toCompletableFuture()
                .thenCompose(c -> {
                    keys.addAll(c.getKeys());
                    if (c.isFinished()) return CompletableFuture.completedFuture(keys);
                    else return scanAllKeys(async, keys, c);
                });
    }

    /**
     * Executes a Redis query asynchronously.
     *
     * @param <R>           the type of the query result
     * @param queryFunction the query
     * @return the query result
     */
    public <R> @NotNull CompletableFuture<R> query(
            final @NotNull Function<RedisAsyncCommands<String, String>, CompletionStage<R>> queryFunction
    ) {
        return queryFunction.apply(connection.async()).toCompletableFuture();
    }

    /**
     * Converts the entity in a string format (uses the internal {@link #mapper}).
     *
     * @param entity the entity
     * @return the serialized entity data
     */
    public @NotNull String serialize(final @NotNull T entity) {
        return mapper.serialize(entity);
    }

    /**
     * Attempts to convert a string into an entity object (uses the internal {@link #mapper}).
     *
     * @param serializedData the serialized entity data
     * @return the entity
     */
    public @NotNull T deserialize(final @NotNull String serializedData) {
        return mapper.deserialize(serializedData, entityMapper.getType());
    }

}
