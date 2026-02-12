package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.api.async.RedisServerAsyncCommands;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of {@link Repository} for Redis databases.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 */
public class RedisRepository<T, ID> extends AbstractRepository<T, ID, RedisQueryEngine<T, ID>> {

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
        return queryEngine.query(async -> async.get(id.toString()))
                .thenApply(Optional::ofNullable)
                .thenApply(o -> o.map(queryEngine::deserialize));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return queryEngine.query(async -> async.exists(id.toString()))
                .thenApply(l -> l > 0);
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T entity) {
        return queryEngine.query(async -> async.set(
                entityMapper.getId(entity).toString(),
                queryEngine.serialize(entity)
        )).thenApply(s -> entity);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id) {
        return queryEngine.query(async -> async.del(id.toString()));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryEngine.getAllKeys().thenCompose(queryEngine::getValues);
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.getValues(ids.stream().map(Object::toString).collect(Collectors.toList()));
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities) {
        return queryEngine.query(async -> async.mset(entities.stream()
                .collect(Collectors.toMap(
                        t -> entityMapper.getId(t).toString(),
                        queryEngine::serialize
                )))
        ).thenApply(s -> entities);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.query(async ->
                async.del(ids.stream().map(Object::toString).toArray(String[]::new))
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query(RedisServerAsyncCommands::dbsize);
    }

}
