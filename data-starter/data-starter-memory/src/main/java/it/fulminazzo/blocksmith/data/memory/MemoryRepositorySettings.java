package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import lombok.*;
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
@With
public final class MemoryRepositorySettings extends CacheRepositorySettings<MemoryRepositorySettings> {
    @Getter
    private @Nullable ExpiryStrategy expirationStrategy;

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
