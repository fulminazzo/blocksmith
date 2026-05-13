package it.fulminazzo.blocksmith.data.redis;

import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Repository settings for Redis repositories.
 *
 * @see RedisRepository
 * @see RedisDataSource
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@With
public final class RedisRepositorySettings extends CacheRepositorySettings<RedisRepositorySettings> {
    private @Nullable String databaseName;
    private @Nullable String collectionName;
    private @Nullable EntityMapper<?, ?> entityMapper;

    /**
     * Sets the entity mapper.
     * <br>
     * <b>NOTE:</b> this call can be avoided when using
     * {@link RedisDataSource#newRepository(EntityMapper, RedisRepositorySettings)}.
     *
     * @param entityMapper the entity mapper
     * @return this object (for method chaining)
     */
    public @NotNull RedisRepositorySettings withEntityMapper(final @NotNull EntityMapper<?, ?> entityMapper) {
        this.entityMapper = entityMapper;
        return this;
    }

    /**
     * Gets the name to use for the database.
     * Together with the {@link #getCollectionName()}, they will form the <b>namespace</b> for the entities
     * in the database with the format: {@code <database_name>:<collection_name>:<id>}.
     *
     * @return the database name
     */
    public @NotNull String getDatabaseName() {
        return Objects.requireNonNull(databaseName, "database name has not been specified yet");
    }

    /**
     * Gets the name to use for the collection.
     * Together with the {@link #getDatabaseName()}, they will form the <b>namespace</b> for the entities
     * in the database with the format: {@code <database_name>:<collection_name>:<id>}.
     *
     * @return the collection name
     */
    public @NotNull String getCollectionName() {
        return Objects.requireNonNull(collectionName, "collection name has not been specified yet");
    }

    /**
     * Gets the entity mapper used to map DTOs.
     *
     * @return the entity mapper
     */
    public @NotNull EntityMapper<?, ?> getEntityMapper() {
        return Objects.requireNonNull(entityMapper, "entity mapper has not been specified yet");
    }

    /**
     * Sets the {@link #entityMapper} if it is not already set.
     *
     * @param entityMapper the entity mapper
     * @return this object (for method chaining)
     */
    @NotNull RedisRepositorySettings withEntityMapperIfNotSet(final @NotNull EntityMapper<?, ?> entityMapper) {
        if (this.entityMapper == null) this.entityMapper = entityMapper;
        return this;
    }

}
