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
public final class MemoryDataSource implements RepositoryDataSource {
    private final @NotNull ExecutorService executor;

    /**
     * Creates a new repository.
     *
     * @param <T>        the type of the entities
     * @param <ID>       the type of the id of the entities
     * @param entityType the entity Java class
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> entityType
    ) {
        return newRepository(EntityMapper.create(entityType));
    }

    /**
     * Creates a new repository.
     *
     * @param <T>          the type of the entities
     * @param <ID>         the type of the id of the entities
     * @param entityMapper the entity mapper
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper
    ) {
        return newRepository(e -> new MemoryRepository<>(e, entityMapper));
    }

    /**
     * Creates a new custom repository.
     *
     * @param <R>               the type of the repository
     * @param <T>               the type of the entities
     * @param <ID>              the type of the id of the entities
     * @param repositoryBuilder the repository creation function
     * @return the repository
     */
    public <R extends Repository<T, ID>, T, ID> @NotNull R newRepository(
            final @NotNull Function<MemoryQueryEngine<T, ID>, R> repositoryBuilder
    ) {
        MemoryQueryEngine<T, ID> engine = new MemoryQueryEngine<>(executor);
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
