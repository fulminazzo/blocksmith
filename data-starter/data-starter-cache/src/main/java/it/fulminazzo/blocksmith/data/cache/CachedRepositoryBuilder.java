package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.*;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.data.memory.MemoryRepository;
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
