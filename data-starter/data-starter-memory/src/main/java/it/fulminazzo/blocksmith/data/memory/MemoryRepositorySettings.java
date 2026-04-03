package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class MemoryRepositorySettings extends CacheRepositorySettings<MemoryRepositorySettings> {

    @Getter
    private @Nullable ExpiryStrategy strategy;

    public @NotNull MemoryRepositorySettings withStrategy(final @NotNull ExpiryStrategy strategy) {
        this.strategy = strategy;
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
