package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.async.RedisServerAsyncCommands;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A basic implementation of {@link Repository} for Redis databases.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 */
public class RedisRepository<T, ID> extends AbstractRepository<T, ID> {
    private final @NotNull StatefulRedisConnection<String, String> connection;
    protected final @NotNull Mapper mapper;

    protected RedisRepository(final @NotNull StatefulRedisConnection<String, String> connection,
                              final @NotNull EntityMapper<T, ID> entityMapper,
                              final @NotNull Mapper mapper) {
        super(entityMapper);
        this.connection = connection;
        this.mapper = mapper;
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return query(async -> async.get(id.toString()))
                .thenApply(Optional::ofNullable)
                .thenApply(o -> o.map(s -> mapper.deserialize(s, entityMapper.getType())));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return query(async -> async.exists(id.toString()))
                .thenApply(l -> l > 0);
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T entity) {
        return query(async -> async.set(
                entityMapper.getId(entity).toString(),
                mapper.serialize(entity)
        )).thenApply(s -> entity);
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
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities) {
        return query(async -> async.mset(entities.stream()
                .collect(Collectors.toMap(
                        t -> entityMapper.getId(t).toString(),
                        mapper::serialize
                )))
        ).thenApply(s -> entities);
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
                .map(v -> mapper.deserialize(v, entityMapper.getType()))
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
