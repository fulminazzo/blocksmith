package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects all the {@link DataSourceFactory} implementations.
 *
 * @see DataSourceConfig
 * @see DataSourceFactory
 * @see RepositoryDataSource
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceFactories {
    private static final @NotNull Map<
            Class<? extends DataSourceConfig>,
            DataSourceFactory
            > FACTORIES = new ConcurrentHashMap<>();

    /**
     * Instantiates a new {@link RepositoryDataSource} from the given configuration.
     *
     * @param dataSourceConfig the data source configuration
     * @return the repository data source
     */
    @SuppressWarnings("unchecked")
    public static @NotNull RepositoryDataSource<RepositorySettings> build(
            final @NotNull DataSourceConfig dataSourceConfig
    ) {
        DataSourceFactory dataSourceFactory = FACTORIES.get(dataSourceConfig.getClass());
        if (dataSourceFactory == null)
            throw new IllegalArgumentException(
                    "No RepositoryDataSource factory currently registered for configuration type: "
                            + dataSourceConfig.getClass().getSimpleName()
            );
        else return (RepositoryDataSource<RepositorySettings>) dataSourceFactory.build(dataSourceConfig);
    }

    /**
     * Registers a new factory for the given {@link DataSourceConfig} type.
     *
     * @param configClass the config class
     * @param factory     the factory
     */
    public static void registerFactory(
            final @NotNull Class<? extends DataSourceConfig> configClass,
            final @NotNull DataSourceFactory factory
    ) {
        FACTORIES.put(configClass, factory);
    }

}
