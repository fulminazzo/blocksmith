package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisServerAsyncCommands;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.CacheRepository;
import it.fulminazzo.blocksmith.data.Page;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.validation.Validator;
import it.fulminazzo.blocksmith.validation.annotation.PositiveOrZero;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Implementation of {@link Repository} for Redis databases.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 */
public class RedisRepository<T, ID> extends AbstractRepository<T, ID, RedisQueryEngine<T, ID>>
        implements CacheRepository<T, ID> {
    private long expiry;

    /**
     * Instantiates a new Redis repository.
     *
     * @param queryEngine  the query engine
     * @param entityMapper the entity mapper
     */
    protected RedisRepository(final @NotNull RedisQueryEngine<T, ID> queryEngine,
                              final @NotNull EntityMapper<T, ID> entityMapper) {
        super(queryEngine, entityMapper);
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return queryEngine.query(async -> async.get(queryEngine.getId(id)))
                .thenApply(Optional::ofNullable)
                .thenApply(o -> o.map(queryEngine::deserialize));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return queryEngine.query(async -> async.exists(queryEngine.getId(id)))
                .thenApply(l -> l > 0);
    }

    @Override
    public @NotNull CompletableFuture<T> saveImpl(final @NotNull T entity) {
        return queryEngine.query(async -> {
            String id = queryEngine.getEntityId(entity);
            String serEntity = queryEngine.serialize(entity);
            if (expiry > 0) return async.psetex(id, expiry, serEntity);
            else return async.set(id, serEntity);
        }).thenApply(s -> entity);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id) {
        return queryEngine.query(async -> async.del(queryEngine.getId(id)));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryEngine.getAllKeys().thenCompose(queryEngine::getValues);
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllImpl(@NotNull Page page) {
        return queryEngine.getAllKeys()
                .thenApply(k -> k.stream()
                        .skip((long) page.getNumber() * page.getSize())
                        .limit(page.getSize())
                        .collect(Collectors.toList()))
                .thenCompose(queryEngine::getValues);
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.getValues(ids.stream().map(queryEngine::getId).collect(Collectors.toList()));
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities) {
        return queryEngine.query(async -> {
            Map<String, String> serEntities = entities.stream()
                    .collect(Collectors.toMap(
                            queryEngine::getEntityId,
                            queryEngine::serialize
                    ));
            CompletionStage<?> future = null;
            if (expiry > 0) {
                Queue<String> queue = new LinkedList<>(serEntities.keySet());
                do {
                    String key = queue.poll();
                    RedisFuture<?> tmp = async.psetex(key, expiry, serEntities.get(key));
                    if (future == null) future = tmp;
                    else future = future.thenCompose(r -> tmp);
                } while (!queue.isEmpty());
            } else future = async.mset(serEntities);
            return future;
        }).thenApply(s -> entities);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.query(async ->
                async.del(ids.stream().map(queryEngine::getId).toArray(String[]::new))
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query(RedisServerAsyncCommands::dbsize);
    }

    @Override
    public @NotNull RedisRepository<T, ID> ttl(final @PositiveOrZero(exceptionMessage = "expire time must be at least 0") @NotNull Duration expiry) {
        Validator.validateMethod(expiry);
        this.expiry = expiry.toMillis();
        return this;
    }

}
