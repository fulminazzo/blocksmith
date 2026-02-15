package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * Common interface for all repositories data sources.
 * <br>
 * Identifies all objects that can create repositories for entities.
 * This is a marker interface - each implementation varies in its methods
 * because each backend has different requirements.
 *
 * @param <S> the type of the repository settings (to build new repositories)
 */
public interface RepositoryDataSource<S extends RepositorySettings> extends Closeable {

    /**
     * Creates a new repository.
     *
     * @param <T>        the type of the entities
     * @param <ID>       the type of the id of the entities
     * @param entityType the entity Java class
     * @param settings   the settings to build the repository with
     * @return the repository
     */
    default <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> entityType,
            final @NotNull S settings
    ) {
        return newRepository(EntityMapper.create(entityType), settings);
    }

    /**
     * Creates a new repository.
     *
     * @param <T>          the type of the entities
     * @param <ID>         the type of the id of the entities
     * @param entityMapper the entity mapper
     * @param settings     the settings to build the repository with
     * @return the repository
     */
    <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull S settings
    );

}
