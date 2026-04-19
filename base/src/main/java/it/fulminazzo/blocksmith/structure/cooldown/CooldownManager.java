package it.fulminazzo.blocksmith.structure.cooldown;

import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;

/**
 * Keeps track of cooldowns for general entities.
 *
 * @param <E> the type of the entity
 */
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public final class CooldownManager<E> implements ICooldownManager<E> {
    private final @NotNull ExpiringMap<E, Boolean> expirations = ExpiringMap.lazy();

    /**
     * Puts the entity on cooldown for the given duration.
     *
     * @param entity   the entity
     * @param cooldown the duration of the cooldown
     * @return this object (for method chaining)
     */
    public @NotNull CooldownManager<E> put(final @NotNull E entity, final @NotNull Duration cooldown) {
        return put(entity, cooldown.toMillis());
    }

    /**
     * Puts the entity on cooldown for the given duration in milliseconds.
     *
     * @param entity   the entity
     * @param cooldown the duration of the cooldown in milliseconds
     * @return this object (for method chaining)
     */
    public @NotNull CooldownManager<E> put(final @NotNull E entity,
                                           final @Range(from = 1, to = Long.MAX_VALUE) long cooldown) {
        if (cooldown <= 0) throw new IllegalArgumentException("cooldown must be positive");
        expirations.put(entity, true, cooldown);
        return this;
    }

    @Override
    public boolean isOnCooldown(final @NotNull E entity) {
        return expirations.containsKey(entity);
    }

    @Override
    public long getRemaining(final @NotNull E entity) {
        Duration expiration = expirations.getTtl(entity);
        if (expiration == null)
            throw new IllegalArgumentException(String.format("Entity '%s' is not on cooldown", entity));
        else return expiration.toMillis();
    }

    @Override
    public @NotNull CooldownManager<E> remove(final @NotNull E entity) {
        expirations.remove(entity);
        return this;
    }

}
