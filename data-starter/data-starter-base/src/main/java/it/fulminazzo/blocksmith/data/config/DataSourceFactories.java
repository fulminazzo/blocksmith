package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceFactories {
    private static final @NotNull Map<Class<? extends DataSourceConfig>, DataSourceFactory> factories = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static @NotNull RepositoryDataSource<RepositorySettings> build(final @NotNull DataSourceConfig dataSourceConfig) {
        DataSourceFactory dataSourceFactory = factories.get(dataSourceConfig.getClass());
        if (dataSourceFactory == null)
            throw new IllegalArgumentException("No RepositoryDataSource factory currently registered for configuration type: " + dataSourceConfig.getClass().getSimpleName());
        return (RepositoryDataSource<RepositorySettings>) dataSourceFactory.build(dataSourceConfig);
    }

    public static void registerFactory(final @NotNull Class<? extends DataSourceConfig> configClass,
                                       final @NotNull DataSourceFactory factory) {
        factories.put(configClass, factory);
    }

}
