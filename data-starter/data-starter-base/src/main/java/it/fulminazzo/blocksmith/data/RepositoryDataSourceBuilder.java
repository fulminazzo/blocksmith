package it.fulminazzo.blocksmith.data;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a general builder for a {@link RepositoryDataSource}.
 *
 * @param <D> the type of the data source
 */
public interface RepositoryDataSourceBuilder<D extends RepositoryDataSource> {

    /**
     * Builds the data source.
     *
     * @return the data source
     */
    @NotNull D build();

}
