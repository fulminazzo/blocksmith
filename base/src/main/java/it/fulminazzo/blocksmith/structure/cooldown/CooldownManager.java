package it.fulminazzo.blocksmith.structure.cooldown;

import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.Map;

/**
 * Keeps track of cooldowns for general entities.
 *
 * @param <E> the type of the entity
 */
public final class CooldownManager<E> {
    private final @NotNull Map<E, Long> expirations = ExpiringMap.lazy();

    /**
     * Checks if the entity is on cooldown.
     *
     * @param entity the entity
     * @return <code>true</code> if it is
     */
    public boolean isOnCooldown(final @NotNull E entity) {
        return expirations.containsKey(entity);
    }

    /**
     * Gets the remaining time in milliseconds until the entity cooldown expires.
     *
     * @param entity the entity
     * @return the remaining time in milliseconds
     * @throws IllegalArgumentException if the entity is not on cooldown
     */
    public long getRemaining(final @NotNull E entity) {
        Long expiration = expirations.get(entity);
        if (expiration == null)
            throw new IllegalArgumentException(String.format("Entity '%s' is not on cooldown", entity));
        else return expiration - now();
    }

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
     * @param cooldown the duration of the cooldown
     * @return this object (for method chaining)
     */
    public @NotNull CooldownManager<E> put(final @NotNull E entity,
                                           final @Range(from = 1, to = Long.MAX_VALUE) long cooldown) {
        if (cooldown <= 0) throw new IllegalArgumentException("cooldown must be at least 1 milliseconds");
        expirations.put(entity, now() + cooldown);
        return this;
    }

    /**
     * Removes the entity from the cooldown.
     *
     * @param entity the entity
     * @return this object (for method chaining)
     */
    public @NotNull CooldownManager<E> remove(final @NotNull E entity) {
        expirations.remove(entity);
        return this;
    }

    private static long now() {
        return System.currentTimeMillis();
    }

}
