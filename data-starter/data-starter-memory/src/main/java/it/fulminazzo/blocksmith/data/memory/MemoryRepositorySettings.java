package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Repository settings for in-memory repositories.
 *
 * @see MemoryRepository
 * @see MemoryDataSource
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemoryRepositorySettings extends CacheRepositorySettings<MemoryRepositorySettings> {
    @Getter
    private @Nullable ExpiryStrategy expirationStrategy;

    /**
     * Sets the expiration strategy.
     *
     * @param strategy the strategy
     * @return this object (for method chaining)
     * @see ExpiryStrategy
     */
    public @NotNull MemoryRepositorySettings withExpirationStrategy(@NotNull ExpiryStrategy strategy) {
        this.expirationStrategy = strategy;
        return this;
    }

    /**
     * Identifies the strategy to use to clear out expired entities.
     * <br>
     * Only valid if {@link #withTtl(Duration)} has been used.
     */
    public enum ExpiryStrategy {
        /**
         * Entities will be kept in memory until the next operation.
         */
        LAZY,
        /**
         * Entities will be removed periodically.
         */
        SCHEDULED

    }

}
