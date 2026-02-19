package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.CacheRepository;
import it.fulminazzo.blocksmith.data.CacheRepositoryDataSource;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * Implementation of {@link RepositoryDataSource} for memory-based repositories.
 * <br>
 * Supports optional TTL (Time To Live) for entries with lazy deletion.
 * Entries automatically expire after a specified duration and are removed when accessed.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         ExecutorService executor = ...;
 *         MemoryDataSource dataSource = MemoryDataSource.create(executor);
 *         }</pre>
 *     </li>
 *     <li>creating a standard repository:
 *         <pre>{@code
 *         MemoryDataSource dataSource = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 EntityMapper.create(User.class),
 *                 new MemoryRepositorySettings()
 *                         .withExpiryInMillis(300000) // optional
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom repository:
 *         <pre>{@code
 *         MemoryDataSource dataSource = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 engine -> new CustomMemoryRepository<>(engine),
 *                 new MemoryRepositorySettings()
 *                         .withExpiryInMillis(300000) // optional
 *         );
 *         }</pre>
 *         where CustomMemoryRepository extends MemoryRepository and adds custom behavior.
 *     </li>
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class MemoryDataSource implements CacheRepositoryDataSource<MemoryRepositorySettings> {
    private final @NotNull ExecutorService executor;

    @Override
    public <T, ID> @NotNull CacheRepository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull MemoryRepositorySettings settings
    ) {
        return newRepository(e -> new MemoryRepository<>(e, entityMapper), settings);
    }

    /**
     * Creates a new custom repository.
     *
     * @param <R>               the type of the repository
     * @param <T>               the type of the entities
     * @param <ID>              the type of the id of the entities
     * @param repositoryBuilder the repository creation function
     * @param settings          the settings to build the repository with
     * @return the repository
     */
    public <T, ID, R extends MemoryRepository<T, ID>> @NotNull R newRepository(
            final @NotNull Function<MemoryQueryEngine<T, ID>, R> repositoryBuilder,
            final @NotNull MemoryRepositorySettings settings
    ) {
        MemoryQueryEngine<T, ID> engine = new MemoryQueryEngine<>(executor);
        Duration ttl = settings.getTtl();
        if (ttl != null) engine.setExpiry(ttl.toMillis());
        return repositoryBuilder.apply(engine);
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    /**
     * Creates a new Memory data source.
     *
     * @param executor the executor
     * @return the memory data source
     */
    public static @NotNull MemoryDataSource create(final @NotNull ExecutorService executor) {
        return new MemoryDataSource(executor);
    }

}
