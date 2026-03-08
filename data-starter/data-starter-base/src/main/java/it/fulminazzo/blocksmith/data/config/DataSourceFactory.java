package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import org.jetbrains.annotations.NotNull;

/**
 * A factory to create a new {@link RepositoryDataSource} from a configuration.
 */
@FunctionalInterface
public interface DataSourceFactory {

    /**
     * Builds the Repository data source.
     *
     * @param <S>    the type of the repository settings
     * @param config the config
     * @return the repository data source
     */
    <S extends RepositorySettings> @NotNull RepositoryDataSource<S> build(final @NotNull DataSourceConfig config);

}
