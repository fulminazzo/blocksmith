package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import org.jetbrains.annotations.NotNull;

/**
 * A factory to create a new {@link RepositoryDataSource} from a configuration.
 */
@FunctionalInterface
public interface DataSourceFactory {

    /**
     * Builds the Repository data source.
     *
     * @param config the config
     * @return the repository data source
     */
    @NotNull RepositoryDataSource<?> build(final @NotNull DataSourceConfig config);

}
