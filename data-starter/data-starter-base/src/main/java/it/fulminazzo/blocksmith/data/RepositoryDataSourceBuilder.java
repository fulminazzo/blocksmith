package it.fulminazzo.blocksmith.data;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a general builder for a {@link RepositoryDataSource}.
 *
 * @param <D> the type of the datasource
 */
public interface RepositoryDataSourceBuilder<D extends RepositoryDataSource> {

    /**
     * Builds the datasource.
     *
     * @return the datasource
     */
    @NotNull D build();

}
