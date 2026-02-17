package it.fulminazzo.blocksmith.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Special implementation of {@link Repository} to support saving of entities
 * with limited lifetime.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 */
public interface CacheRepository<T, ID> extends Repository<T, ID> {

    /**
     * Sets the expiration time when saving an entity.
     *
     * @param expiry the expiration time (in milliseconds)
     * @return this object (for method chaining)
     */
    @NotNull CacheRepository<T, ID> setExpiry(final @Range(from = 0, to = Long.MAX_VALUE) long expiry);

}
