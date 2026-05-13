package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * Common interface for all the data sources of the repositories.
 * <br>
 * Identifies all objects that can create repositories for entities.
 * This is a marker interface - each implementation varies in its methods
 * because each backend has different requirements.
 *
 * @param <S> the type of the repository settings (to build new repositories)
 * @see CacheRepositoryDataSource
 * @see RepositoryDataSourceBuilder
 * @see Repository
 * @see RepositorySettings
 * @see QueryEngine
 * @see EntityMapper
 */
public interface RepositoryDataSource<S extends RepositorySettings> extends Closeable {

    /**
     * Creates a new repository.
     *
     * @param <T>        the type of the entities
     * @param <I>        the type of the id of the entities
     * @param entityType the entity Java class
     * @param settings   the settings to build the repository with
     * @return the repository
     */
    default <T, I> @NotNull Repository<T, I> newRepository(
            final @NotNull Class<T> entityType,
            final @NotNull S settings
    ) {
        return newRepository(EntityMapper.create(entityType), settings);
    }

    /**
     * Creates a new repository.
     *
     * @param <T>          the type of the entities
     * @param <I>          the type of the id of the entities
     * @param entityMapper the entity mapper
     * @param settings     the settings to build the repository with
     * @return the repository
     */
    <T, I> @NotNull Repository<T, I> newRepository(
            final @NotNull EntityMapper<T, I> entityMapper,
            final @NotNull S settings
    );

}
