package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.cache.CachedDataSource;
import it.fulminazzo.blocksmith.data.cache.CachedRepositorySettings;
import it.fulminazzo.blocksmith.data.file.FileDataSource;
import it.fulminazzo.blocksmith.data.file.FileRepositorySettings;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings;
import it.fulminazzo.blocksmith.data.mongodb.MongoDataSource;
import it.fulminazzo.blocksmith.data.mongodb.MongoRepositorySettings;
import it.fulminazzo.blocksmith.data.redis.RedisDataSource;
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings;
import it.fulminazzo.blocksmith.data.sql.SqlDataSource;
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * This is a special class to encapsulate all the {@link RepositorySettings} implementations.
 * <br>
 * Then, only the interested repository, according to the Datasource, is taken.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder
public final class AllRepositorySettings {

    @NotNull MemoryRepositorySettings memory;

    @NotNull FileRepositorySettings file;

    @NotNull SqlRepositorySettings sql;

    @NotNull RedisRepositorySettings redis;

    @NotNull MongoRepositorySettings mongo;

    /**
     * Converts the current settings to a {@link RepositorySettings}, according to the given Data source.
     *
     * @param dataSource the data source
     * @return the repository settings
     */
    public @NotNull RepositorySettings getRepositorySettings(final @NotNull RepositoryDataSource<?> dataSource) {
        if (dataSource instanceof MemoryDataSource) return memory;
        else if (dataSource instanceof FileDataSource) return file;
        else if (dataSource instanceof SqlDataSource) return sql;
        else if (dataSource instanceof RedisDataSource) return redis;
        else if (dataSource instanceof MongoDataSource) return mongo;
        else if (dataSource instanceof CachedDataSource) return getCachedRepositorySettings(dataSource);
        else throw new IllegalArgumentException("Unsupported data source type: " + dataSource.getClass().getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    private <
            CS extends CacheRepositorySettings<CS>,
            S extends RepositorySettings
            > @NotNull CachedRepositorySettings<CS, S> getCachedRepositorySettings(final @NotNull RepositoryDataSource<?> dataSource) {
        Reflect reflect = Reflect.on(dataSource);
        RepositoryDataSource<?> cacheRepositoryDataSource = reflect.get("cacheRepositoryDataSource").get();
        RepositoryDataSource<?> repositoryDataSource = reflect.get("repositoryDataSource").get();
        return CachedRepositorySettings.combine(
                (CS) getRepositorySettings(cacheRepositoryDataSource),
                (S) getRepositorySettings(repositoryDataSource)
        );
    }

}
