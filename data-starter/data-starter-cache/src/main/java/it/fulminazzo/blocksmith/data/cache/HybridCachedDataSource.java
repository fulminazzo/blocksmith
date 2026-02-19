package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.CacheRepositoryDataSource;
import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings;
import org.jetbrains.annotations.NotNull;

/**
 * A special {@link CachedDataSource} that uses two caches to lookup data.
 * Check {@link CachedDataSource#hybrid(MemoryDataSource, CacheRepositoryDataSource, RepositoryDataSource)} for more.
 *
 * @param <CS> the repository settings of the cache repository data source
 * @param <S>  the repository settings of the internal repository data source
 */
public final class HybridCachedDataSource<
        CS extends CacheRepositorySettings<CS>,
        S extends RepositorySettings
        > extends CachedDataSource<MemoryRepositorySettings, CachedRepositorySettings<CS, S>> {

    /**
     * Instantiates a new Hybrid cached data source.
     *
     * @param cacheDataSource      the cache repositories data source
     * @param repositoryDataSource the actual repositories data source
     */
    HybridCachedDataSource(final @NotNull CacheRepositoryDataSource<MemoryRepositorySettings> cacheDataSource,
                           final @NotNull RepositoryDataSource<CachedRepositorySettings<CS, S>> repositoryDataSource) {
        super(cacheDataSource, repositoryDataSource);
    }

}
