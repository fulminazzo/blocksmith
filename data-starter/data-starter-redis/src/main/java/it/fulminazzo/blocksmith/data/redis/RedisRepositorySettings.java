package it.fulminazzo.blocksmith.data.redis;

import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class RedisRepositorySettings extends CacheRepositorySettings<RedisRepositorySettings> {
    private @Nullable String databaseName;
    private @Nullable String collectionName;
    private @Nullable EntityMapper<?, ?> entityMapper;

    public @NotNull String getDatabaseName() {
        return Objects.requireNonNull(databaseName, "database name has not been specified yet");
    }

    public @NotNull String getCollectionName() {
        return Objects.requireNonNull(collectionName, "collection name has not been specified yet");
    }

    public @NotNull EntityMapper<?, ?> getEntityMapper() {
        return Objects.requireNonNull(entityMapper, "entity mapper has not been specified yet");
    }

    public @NotNull RedisRepositorySettings withDatabaseName(final @NotNull String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public @NotNull RedisRepositorySettings withCollectionName(final @NotNull String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    /**
     * Sets the entity mapper.
     * <br>
     * <b>NOTE:</b> this call can be avoided when using
     * {@link RedisDataSource#newRepository(Class, RepositorySettings)}
     * and {@link RedisDataSource#newRepository(EntityMapper, RedisRepositorySettings)}
     *
     * @param entityMapper the entity mapper
     * @return this object (for method chaining)
     */
    public @NotNull RedisRepositorySettings withEntityMapper(final @NotNull EntityMapper<?, ?> entityMapper) {
        this.entityMapper = entityMapper;
        return this;
    }

    @NotNull RedisRepositorySettings withEntityMapperIfNotSet(final @NotNull EntityMapper<?, ?> entityMapper) {
        if (this.entityMapper == null) this.entityMapper = entityMapper;
        return this;
    }

}
