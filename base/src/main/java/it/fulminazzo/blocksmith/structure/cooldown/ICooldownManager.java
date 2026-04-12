package it.fulminazzo.blocksmith.structure.cooldown;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a general interface to handle cooldowns for general entities.
 *
 * @param <E> the type of the entity
 */
public interface ICooldownManager<E> {

    /**
     * Checks if the entity is on cooldown.
     *
     * @param entity the entity
     * @return <code>true</code> if it is
     */
    boolean isOnCooldown(final @NotNull E entity);

    /**
     * Gets the remaining time in milliseconds until the entity cooldown expires.
     *
     * @param entity the entity
     * @return the remaining time in milliseconds
     * @throws IllegalArgumentException if the entity is not on cooldown
     */
    long getRemaining(final @NotNull E entity);

    /**
     * Removes the entity from the cooldown.
     *
     * @param entity the entity
     * @return this object (for method chaining)
     */
    @NotNull ICooldownManager<E> remove(final @NotNull E entity);

}
