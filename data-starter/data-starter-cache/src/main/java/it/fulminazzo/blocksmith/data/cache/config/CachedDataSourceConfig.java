package it.fulminazzo.blocksmith.data.cache.config;

import it.fulminazzo.blocksmith.data.CacheRepositoryDataSource;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.cache.CachedDataSource;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class CachedDataSourceConfig implements DataSourceConfig {

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
    @NonNull
    DataSourceConfig cache;

    @NotNull
    @NonNull
    DataSourceConfig repository;

    @Nullable
    Boolean hybrid;

}
