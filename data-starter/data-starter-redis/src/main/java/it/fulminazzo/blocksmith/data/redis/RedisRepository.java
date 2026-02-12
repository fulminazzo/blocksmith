package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.async.RedisServerAsyncCommands;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A basic implementation of {@link Repository} for Redis databases.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (will be used as file names)
 */
@RequiredArgsConstructor
public class RedisRepository<T, ID> extends AbstractRepository<T, ID> {
    private final @NotNull StatefulRedisConnection<String, String> connection;
    private final @NotNull Function<T, ID> idMapper;
    private final @NotNull Class<T> dataType;
    protected final @NotNull Mapper mapper;

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return query(async -> async.get(id.toString()))
                .thenApply(Optional::ofNullable)
                .thenApply(o -> o.map(s -> mapper.deserialize(s, dataType)));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return query(async -> async.exists(id.toString()))
                .thenApply(l -> l > 0);
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T data) {
        return query(async -> async.set(
                idMapper.apply(data).toString(),
                mapper.serialize(data)
        )).thenApply(s -> data);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id) {
        return query(async -> async.del(id.toString()));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return getAllKeys().thenCompose(this::getValues);
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return getValues(ids.stream().map(Object::toString).collect(Collectors.toList()));
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entries) {
        return query(async -> async.mset(entries.stream()
                .collect(Collectors.toMap(
                        t -> idMapper.apply(t).toString(),
                        mapper::serialize
                )))
        ).thenApply(s -> entries);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return query(async ->
                async.del(ids.stream().map(Object::toString).toArray(String[]::new))
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return query(RedisServerAsyncCommands::dbsize);
    }

    /**
     * Queries the database to get all the values of the corresponding keys.
     *
     * @param keys the keys
     * @return the values
     */
    protected @NotNull CompletableFuture<Collection<T>> getValues(final @NotNull Collection<String> keys) {
        return query(async ->
                async.mget(keys.toArray(String[]::new))
        ).thenApply(l -> l.stream()
                .filter(KeyValue::hasValue)
                .map(KeyValue::getValue)
                .map(v -> mapper.deserialize(v, dataType))
                .collect(Collectors.toList())
        );
    }

    /**
     * Gets all the currently stored keys in the database.
     *
     * @return the keys
     */
    protected @NotNull CompletableFuture<Collection<String>> getAllKeys() {
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
