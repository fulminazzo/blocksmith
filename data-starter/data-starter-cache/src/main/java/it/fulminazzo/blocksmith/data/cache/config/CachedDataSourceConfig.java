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
import org.jetbrains.annotations.Nullable;

/**
 * {@link DataSourceConfig} for {@link CachedDataSource}.
 *
 * @see DataSourceConfig
 * @see CachedDataSource
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@With
public final class CachedDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                CachedDataSourceConfig.class,
                c -> {
                    CachedDataSourceConfig config = (CachedDataSourceConfig) c;
                    CacheRepositoryDataSource<?> cache = (CacheRepositoryDataSource<?>)
                            DataSourceFactories.build(config.getCache());
                    RepositoryDataSource<?> repository = DataSourceFactories.build(config.getRepository());
                    return Boolean.TRUE.equals(config.getHybrid())
                            ? CachedDataSource.hybrid(
                            MemoryDataSource.createAsync(),
                            cache,
                            repository
                    ) : CachedDataSource.create(cache, repository);
                }
        );
    }

    @NonNull
    DataSourceConfig cache;

    @NonNull
    DataSourceConfig repository;

    @Nullable
    Boolean hybrid;

}
