package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.*;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.data.memory.MemoryRepository;
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A builder for creating {@link CachedRepository} objects.
 * <br>
 * The following examples will utilize a <b>SQL</b> as main repository (with the support of the
 * <a href="https://www.jooq.org/doc/latest/manual/code-generation/codegen-execution/codegen-gradle/">jOOQ generator plugin</a>
 * and <b>Redis</b> as cache.
 * <ul>
 *     <li>creating a repository:
 *         <pre>{@code
 *         SqlDataSource dataSource = ...;
 *         RedisDataSource cacheDataSource = ...;
 *         Repository<?, ?> mainRepository = dataSource.newRepository(
 *                 EntityMapper.create(User.class),
 *                 new SqlRepositorySettings()
 *                         .withTable(Tables.USERS)
 *                         .withIdColumn(Tables.USERS.ID)
 *         );
 *         Repository<?, ?> repository = CachedRepository.wrap(mainRepository)
 *                 // not necessary if using implementations of the default repositories as main repository
 *                 .entityMapper(EntityMapper.create(User.class))
 *                 .cacheRepository(
 *                         cacheDataSource,
 *                         new RedisRepositorySettings()
 *                                 .withDatabaseName("database")
 *                                 .withCollectionName("users")
 *                                 .withTtl(Duration.ofMinutes(30))
 *                 )
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creating a hybrid repository (will look up an in-memory cache before querying for the cache):
 *         <pre>{@code
 *         SqlDataSource dataSource = ...;
 *         RedisDataSource cacheDataSource = ...;
 *         MemoryDataSource memoryDataSource = ...;
 *         Repository<?, ?> mainRepository = dataSource.newRepository(
 *                 EntityMapper.create(User.class),
 *                 new SqlRepositorySettings()
 *                         .withTable(Tables.USERS)
 *                         .withIdColumn(Tables.USERS.ID)
 *         );
 *         Repository<?, ?> repository = CachedRepository.wrap(mainRepository)
 *                 // not necessary if using implementations of the default repositories as main repository
 *                 .entityMapper(EntityMapper.create(User.class))
 *                 .cacheRepository(
 *                         cacheDataSource,
 *                         new RedisRepositorySettings()
 *                                 .withDatabaseName("database")
 *                                 .withCollectionName("users")
 *                                 .withTtl(Duration.ofMinutes(30))
 *                 )
 *                 .hybrid(
 *                         memoryDataSource,
 *                         new MemoryRepositorySettings()
 *                                 .withTtl(Duration.ofMinutes(5))
 *                 );
 *         }</pre>
 *     </li>
 * </ul>
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities
 */
public final class CachedRepositoryBuilder<T, ID> {
    private final @NotNull Repository<T, ID> internalRepository;

    private @Nullable EntityMapper<T, ID> entityMapper;

    private @Nullable CacheRepository<T, ID> cacheRepository;

    /**
     * Instantiates a new Cached repository builder.
     *
     * @param internalRepository the internal repository
     */
    CachedRepositoryBuilder(final @NotNull Repository<T, ID> internalRepository) {
        this.internalRepository = internalRepository;
        if (internalRepository instanceof AbstractRepository<?, ?, ?>)
            entityMapper(((AbstractRepository<T, ID, ?>) internalRepository).getEntityMapper());
    }

    /**
     * Builds a Cached repository with two caches.
     * The lookup logic is the following:
     * <ol>
     *     <li>a {@link it.fulminazzo.blocksmith.data.memory.MemoryRepository} is queried for the resource;</li>
     *     <li>if not found, the {@link CachedRepository} is queried for the resource;</li>
     *     <li>if not found, the actual {@link Repository} is queried.</li>
     * </ol>
     * This process allows for faster lookups when querying multiple data.
     *
     * @param memoryDataSource         the memory data source to create the memory repository from
     * @param memoryRepositorySettings the memory repository settings
     * @return the repository
     */
    public @NotNull Repository<T, ID> hybrid(
            final @NotNull CacheRepositoryDataSource<MemoryRepositorySettings> memoryDataSource,
            final @NotNull MemoryRepositorySettings memoryRepositorySettings
    ) {
        return new CachedRepository<>(
                memoryDataSource.newRepository(getEntityMapper(), memoryRepositorySettings),
                build(),
                getEntityMapper()
        );
    }

    /**
     * Builds a Cached repository with two caches.
     * The lookup logic is the following:
     * <ol>
     *     <li>a {@link it.fulminazzo.blocksmith.data.memory.MemoryRepository} is queried for the resource;</li>
     *     <li>if not found, the {@link CachedRepository} is queried for the resource;</li>
     *     <li>if not found, the actual {@link Repository} is queried.</li>
     * </ol>
     * This process allows for faster lookups when querying multiple data.
     *
     * @param memoryRepository the memory repository
     * @return the repository
     */
    public @NotNull Repository<T, ID> hybrid(final @NotNull MemoryRepository<T, ID> memoryRepository) {
        return new CachedRepository<>(
                memoryRepository,
                build(),
                getEntityMapper()
        );
    }

    /**
     * Builds the Cached repository.
     *
     * @return the repository
     */
    public @NotNull Repository<T, ID> build() {
        return new CachedRepository<>(
                Objects.requireNonNull(cacheRepository, "cacheRepository has not been specified yet"),
                internalRepository,
                getEntityMapper()
        );
    }

    /**
     * Sets the repository to use as cache.
     * <br>
     * Uses the provided data source to create a new repository.
     *
     * @param <S>                the type of the cache repository settings
     * @param dataSource         the data source to create the cache repository from
     * @param repositorySettings the cache repository settings
     * @return this object (for method chaining)
     */
    public <S extends CacheRepositorySettings<S>> @NotNull CachedRepositoryBuilder<T, ID> cacheRepository(
            final @NotNull CacheRepositoryDataSource<S> dataSource,
            final @NotNull S repositorySettings
    ) {
        return cacheRepository(
                dataSource.newRepository(getEntityMapper(), repositorySettings)
        );
    }

    /**
     * Sets the repository to use as cache.
     *
     * @param cacheRepository the repository
     * @return this object (for method chaining)
     */
    public @NotNull CachedRepositoryBuilder<T, ID> cacheRepository(final @NotNull CacheRepository<T, ID> cacheRepository) {
        this.cacheRepository = cacheRepository;
        return this;
    }

    /**
     * Sets the type of the entities for the internal mapper.
     * <br>
     * This is NOT necessary if the provided internal repository is one of the modules implementations.
     *
     * @param type the entity Java class
     * @return this object (for method chaining)
     */
    public @NotNull CachedRepositoryBuilder<T, ID> entityType(final @NotNull Class<T> type) {
        this.entityMapper = EntityMapper.create(type);
        return this;
    }

    /**
     * Sets the entity mapper.
     * <br>
     * This is NOT necessary if the provided internal repository is one of the modules implementations.
     *
     * @param entityMapper the entity mapper
     * @return this object (for method chaining)
     */
    public @NotNull CachedRepositoryBuilder<T, ID> entityMapper(final @NotNull EntityMapper<T, ID> entityMapper) {
        this.entityMapper = entityMapper;
        return this;
    }

    private @NotNull EntityMapper<T, ID> getEntityMapper() {
        return Objects.requireNonNull(entityMapper, "entityMapper has not been specified yet");
    }

}
