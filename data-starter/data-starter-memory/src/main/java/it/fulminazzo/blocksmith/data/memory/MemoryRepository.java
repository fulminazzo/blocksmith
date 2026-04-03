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
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities
 */
public class MemoryRepository<T, ID> extends AbstractRepository<T, ID, MemoryQueryEngine<T, ID>>
        implements CacheRepository<T, ID> {
    private @Nullable Duration expiry;

    /**
     * Instantiates a new Memory repository.
     *
     * @param queryEngine  the query engine
     * @param entityMapper the entity mapper
     */
    public MemoryRepository(final @NotNull MemoryQueryEngine<T, ID> queryEngine,
                            final @NotNull EntityMapper<T, ID> entityMapper) {
        super(queryEngine, entityMapper);
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return queryEngine.query(m -> m.get(id)).thenApply(Optional::ofNullable);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return queryEngine.query(m -> m.containsKey(id));
    }

    @Override
    protected @NotNull CompletableFuture<T> saveImpl(final @NotNull T entity) {
        return queryEngine.query(m -> {
            ID id = entityMapper.getId(entity);
            if (expiry == null) m.put(id, entity);
            else m.put(id, entity, expiry);
            return entity;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id) {
        return queryEngine.query(m -> m.remove(id));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryEngine.query(Map::values);
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
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
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
                ID id = entityMapper.getId(e);
                if (expiry == null) m.put(id, e);
                else m.put(id, e, expiry);
            });
            return entities;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.query(m -> {
            ids.forEach(m::remove);
            return null;
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query(m -> (long) m.size());
    }

    @Override
    public @NotNull MemoryRepository<T, ID> ttl(final @Nullable Duration expiry) {
        this.expiry = expiry;
        return this;
    }

    /**
     * Creates a new Memory repository.
     * <br>
     * Every query will be run <b>synchronously</b>.
     *
     * @param <T>        the type of the entities
     * @param <ID>       the type of the id of the entities
     * @param entityType the entity Java class
     * @return the repository
     */
    public static <T, ID> @NotNull MemoryRepository<T, ID> create(final @NotNull Class<T> entityType) {
        return create(EntityMapper.create(entityType));
    }

    /**
     * Creates a new Memory repository.
     * <br>
     * Every query will be run <b>synchronously</b>.
     *
     * @param <T>          the type of the entities
     * @param <ID>         the type of the id of the entities
     * @param entityMapper the entity mapper
     * @return the repository
     */
    public static <T, ID> @NotNull MemoryRepository<T, ID> create(final @NotNull EntityMapper<T, ID> entityMapper) {
        return new MemoryRepository<>(
                new MemoryQueryEngine<>(ExpiringMap.lazy(), Runnable::run),
                entityMapper
        );
    }

}
