package it.fulminazzo.blocksmith.data.cache.config;

import it.fulminazzo.blocksmith.data.CacheRepositoryDataSource;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.cache.CachedDataSource;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;

@Value
public class CachedDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                CachedDataSourceConfig.class,
                c -> {
                    CachedDataSourceConfig config = (CachedDataSourceConfig) c;
                    CacheRepositoryDataSource<?> cache = (CacheRepositoryDataSource<?>) DataSourceFactories.build(config.getCache());
                    RepositoryDataSource<?> repository = DataSourceFactories.build(config.getRepository());
                    return Boolean.TRUE.equals(config.getHybrid())
                            ? CachedDataSource.hybrid(MemoryDataSource.create(Executors.newCachedThreadPool()), cache, repository)
                            : CachedDataSource.create(cache, repository);
                }
        );
    }

    @NotNull
    @org.jetbrains.annotations.NotNull
    DataSourceConfig cache;

    @NotNull
    @org.jetbrains.annotations.NotNull
    DataSourceConfig repository;

    @Nullable
    Boolean hybrid;

}
