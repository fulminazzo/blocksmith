package it.fulminazzo.blocksmith.data;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Special implementation of {@link Repository} to support saving of entities with limited lifetime.
 *
 * @param <T> the type of the entities
 * @param <I> the type of the id of the entities (should be unique)
 * @see Repository
 * @see CacheRepositorySettings
 * @see CacheRepositoryDataSource
 */
public interface CacheRepository<T, I> extends Repository<T, I> {

    /**
     * Sets the expiration time when saving an entity.
     *
     * @param expiry the expiration time
     * @return this object (for method chaining)
     */
    @NotNull CacheRepository<T, I> ttl(final @NotNull Duration expiry);

}
