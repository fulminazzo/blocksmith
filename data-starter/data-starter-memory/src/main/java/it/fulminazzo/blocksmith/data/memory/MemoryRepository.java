package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.CacheRepository;
import it.fulminazzo.blocksmith.data.Page;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of {@link Repository} that stores data in memory (RAM).
 * <br>
 * Examples:
 * <ul>
 *     <li>creation of simple:
 *         <pre>{@code
 *         MemoryRepository<T, ID> repository = MemoryRepository.create(User.class);
 *         }</pre>
 *         <b>WARNING:</b> every query will be run <b>synchronously</b>.
 *     </li>
 * </ul>
 *
 * @param <T> the type of the entities
 * @param <I> the type of the id of the entities
 * @see MemoryRepositorySettings
 * @see MemoryQueryEngine
 */
public class MemoryRepository<T, I> extends AbstractRepository<T, I, MemoryQueryEngine<T, I>>
        implements CacheRepository<T, I> {
    private @Nullable Duration expiry;

    /**
     * Instantiates a new Memory repository.
     *
     * @param queryEngine  the query engine
     * @param entityMapper the entity mapper
     */
    public MemoryRepository(
            final @NotNull MemoryQueryEngine<T, I> queryEngine,
            final @NotNull EntityMapper<T, I> entityMapper
    ) {
        super(queryEngine, entityMapper);
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query(m -> (long) m.size());
    }

    @Override
    public @NotNull MemoryRepository<T, I> ttl(final @Nullable Duration expiry) {
        this.expiry = expiry;
        return this;
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull I id) {
        return queryEngine.query(m -> m.get(id)).thenApply(Optional::ofNullable);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull I id) {
        return queryEngine.query(m -> m.containsKey(id));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryEngine.query(Map::values);
    }

    @Override
    protected @NotNull CompletableFuture<T> saveImpl(final @NotNull T entity) {
        return queryEngine.query(m -> {
            I id = entityMapper.getId(entity);
            if (expiry == null) m.put(id, entity);
            else m.put(id, entity, expiry);
            return entity;
        });
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllImpl(final @NotNull Page page) {
        return queryEngine.query(m -> m.values().stream()
                .skip((long) page.getNumber() * page.getSize())
                .limit(page.getSize())
                .collect(Collectors.toList())
        );
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<I> ids) {
        return queryEngine.query(m -> ids.stream()
                .map(m::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities) {
        return queryEngine.query(m -> {
            entities.forEach(e -> {
                I id = entityMapper.getId(e);
                if (expiry == null) m.put(id, e);
                else m.put(id, e, expiry);
            });
            return entities;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<I> ids) {
        return queryEngine.query(m -> {
            ids.forEach(m::remove);
            return null;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull I id) {
        return queryEngine.query(m -> m.remove(id));
    }

    /**
     * Creates a new Memory repository.
     * <br>
     * Every query will be run <b>synchronously</b>.
     *
     * @param <T>        the type of the entities
     * @param <I>        the type of the id of the entities
     * @param entityType the entity Java class
     * @return the repository
     */
    public static <T, I> @NotNull MemoryRepository<T, I> create(final @NotNull Class<T> entityType) {
        return create(EntityMapper.create(entityType));
    }

    /**
     * Creates a new Memory repository.
     * <br>
     * Every query will be run <b>synchronously</b>.
     *
     * @param <T>          the type of the entities
     * @param <I>          the type of the id of the entities
     * @param entityMapper the entity mapper
     * @return the repository
     */
    public static <T, I> @NotNull MemoryRepository<T, I> create(final @NotNull EntityMapper<T, I> entityMapper) {
        return new MemoryRepository<>(
                new MemoryQueryEngine<>(ExpiringMap.lazy(), Runnable::run),
                entityMapper
        );
    }

}
