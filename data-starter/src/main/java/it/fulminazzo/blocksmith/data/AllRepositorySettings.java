package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.cache.CachedDataSource;
import it.fulminazzo.blocksmith.data.cache.CachedRepositorySettings;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
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
 * This is a special class encapsulating all {@link RepositorySettings} implementations.
 * The correct settings are then picked up according to the provided {@link RepositoryDataSource}.
 * <br>
 * This ensures a Write-Once-Run-Everywhere approach and gives the developer the possibility to
 * integrate with any data source provided.
 * <br>
 * Imagine this scenario: the developer loads a {@link DataSourceConfig} provided from the user
 * (via a configuration file). It is impossible to know prior the type of {@link RepositoryDataSource}
 * that will be created (unless only one {@code data-starter} module is being used).
 * Therefore, this class provides support for all possible combinations and implementations,
 * so that the developer does not have to worry about the underlying database and can deal with the
 * {@code data-starter} API with ease.
 * <br>
 * Example:
 * <pre>{@code
 * final MemoryRepositorySettings memoryRepositorySettings = ...;
 * final FileRepositorySettings fileRepositorySettings = ...;
 * final SqlRepositorySettings sqlRepositorySettings = ...;
 * final RedisRepositorySettings redisRepositorySettings = ...;
 * final MongoRepositorySettings mongoRepositorySettings = ...;
 *
 * final AllRepositorySettings repositorySettings = AllRepositorySettings.builder()
 *         .memory(memoryRepositorySettings)
 *         .file(fileRepositorySettings)
 *         .sql(sqlRepositorySettings)
 *         .redis(redisRepositorySettings)
 *         .mongo(mongoRepositorySettings)
 *         .build();
 *
 * DataSourceConfig dataSourceConfig = ...; // loaded from a configuration provider
 * RepositoryDataSource<RepositorySettings> dataSource = DataSourceFactories.build(dataSourceConfig);
 *
 * // We don't care what the data source settings type are, they are fetched automatically
 * Repository<?, ?> repository = dataSource.newRepository(
 *         EntityMapper.create(User.class),
 *         repositorySettings.getRepositorySettings(dataSource)
 * );
 * }</pre>
 *
 * @see RepositorySettings
 * @see RepositoryDataSource
 * @see DataSourceConfig
 * @see DataSourceFactories
 */
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
        else throw new IllegalArgumentException("Unsupported data source type: "
                    + dataSource.getClass().getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    private <
            C extends CacheRepositorySettings<C>,
            S extends RepositorySettings
            > @NotNull CachedRepositorySettings<C, S> getCachedRepositorySettings(
            final @NotNull RepositoryDataSource<?> dataSource
    ) {
        Reflect reflect = Reflect.on(dataSource);
        RepositoryDataSource<?> cacheRepositoryDataSource = reflect.get("cacheRepositoryDataSource").get();
        RepositoryDataSource<?> repositoryDataSource = reflect.get("repositoryDataSource").get();
        return CachedRepositorySettings.combine(
                (C) getRepositorySettings(cacheRepositoryDataSource),
                (S) getRepositorySettings(repositoryDataSource)
        );
    }

}
