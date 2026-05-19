package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;

/**
 * Special implementation of {@link RepositoryDataSource} for creating cache repositories.
 *
 * @param <S> the type of the repository settings (to build new repositories)
 * @see RepositoryDataSource
 * @see RepositoryDataSourceBuilder
 * @see CacheRepository
 * @see CacheRepositorySettings
 * @see QueryEngine
 * @see EntityMapper
 */
public interface CacheRepositoryDataSource<S extends CacheRepositorySettings<S>> extends RepositoryDataSource<S> {

    @SuppressWarnings("unchecked")
    @Override
    default <T, I> @NotNull CacheRepository<T, I> newRepository(
            final @NotNull Class<T> entityType,
            final @NotNull S settings
    ) {
        return (CacheRepository<T, I>) RepositoryDataSource.super.newRepository(entityType, settings);
    }

    @Override
    <T, I> @NotNull CacheRepository<T, I> newRepository(
            final @NotNull EntityMapper<T, I> entityMapper,
            final @NotNull S settings
    );

}
