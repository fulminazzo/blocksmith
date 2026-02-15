package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * Implementation of {@link RepositoryDataSource} for memory-based repositories.
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         ExecutorService executor = ...;
 *         MemoryDataSource dataSource = MemoryDataSource.create(executor);
 *         }</pre>
 *     </li>
 *     <li>creating a new repository:
 *         <pre>{@code
 *         MemoryDataSource dataSource = ...;
 *         Class<?> dataType = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(dataType);
 *         }</pre>
 *         or, for more control:
 *         <pre>{@code
 *         Repository<?, ?> repository = dataSource.newRepository(EntityMapper.create(dataType, "idFieldName"));
 *         }</pre>
 *     </li>
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class MemoryDataSource implements RepositoryDataSource<MemoryRepositorySettings> {
    private final @NotNull ExecutorService executor;

    @Override
    public <T, ID> @NotNull Repository<T, ID> newRepository(
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
    public <R extends Repository<T, ID>, T, ID> @NotNull R newRepository(
            final @NotNull Function<MemoryQueryEngine<T, ID>, R> repositoryBuilder,
            final @NotNull MemoryRepositorySettings settings
    ) {
        MemoryQueryEngine<T, ID> engine = new MemoryQueryEngine<>(executor);
        engine.setExpiry(settings.getExpiryInMillis());
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
     * @return the file data source
     */
    public static @NotNull MemoryDataSource create(final @NotNull ExecutorService executor) {
        return new MemoryDataSource(executor);
    }

}
