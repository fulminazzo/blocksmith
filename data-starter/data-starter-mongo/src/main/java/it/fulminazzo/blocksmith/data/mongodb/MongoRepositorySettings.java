package it.fulminazzo.blocksmith.data.mongodb;

import it.fulminazzo.blocksmith.data.RepositorySettings;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Repository settings for MongoDB repositories.
 *
 * @see MongoRepository
 * @see MongoDataSource
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@With
public final class MongoRepositorySettings extends RepositorySettings {
    private @Nullable String databaseName;
    private @Nullable String collectionName;
    private @Nullable EntityMapper<?, ?> entityMapper;

    /**
     * Sets the entity mapper.
     * <br>
     * <b>NOTE:</b> this call can be avoided when using
     * {@link MongoDataSource#newRepository(EntityMapper, MongoRepositorySettings)}.
     *
     * @param entityMapper the entity mapper
     * @return this object (for method chaining)
     */
    public @NotNull MongoRepositorySettings withEntityMapper(final @NotNull EntityMapper<?, ?> entityMapper) {
        this.entityMapper = entityMapper;
        return this;
    }

    /**
     * Gets the database name.
     *
     * @return the database name
     */
    public @NotNull String getDatabaseName() {
        return Objects.requireNonNull(databaseName, "database name has not been specified yet");
    }

    /**
     * Gets the collection name.
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
    @NotNull MongoRepositorySettings withEntityMapperIfNotSet(final @NotNull EntityMapper<?, ?> entityMapper) {
        if (this.entityMapper == null) this.entityMapper = entityMapper;
        return this;
    }

}
