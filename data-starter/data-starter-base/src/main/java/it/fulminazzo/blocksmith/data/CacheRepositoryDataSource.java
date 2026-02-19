package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;

/**
 * Special implementation of {@link RepositoryDataSource} for creating cache repositories.
 *
 * @param <S> the type of the repository settings (to build new repositories)
 */
public interface CacheRepositoryDataSource<S extends CacheRepositorySettings<S>> extends RepositoryDataSource<S> {

    @SuppressWarnings("unchecked")
    @Override
    default <T, ID> @NotNull CacheRepository<T, ID> newRepository(
            final @NotNull Class<T> entityType,
            final @NotNull S settings
    ) {
        return (CacheRepository<T, ID>) RepositoryDataSource.super.newRepository(entityType, settings);
    }

    @Override
    <T, ID> @NotNull CacheRepository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull S settings
    );

}
