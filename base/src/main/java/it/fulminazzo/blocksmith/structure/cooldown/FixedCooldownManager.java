package it.fulminazzo.blocksmith.structure.cooldown;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;

/**
 * A manager to store entities on cooldown.
 * It is possible to verify if the entity is on cooldown and to retrieve the remaining time for general purposes.
 * <br>
 * Each entity is put with the same cooldown specified upon creation of the manager.
 *
 * @param <E> the type of the entity
 * @see CooldownManager
 */
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public final class FixedCooldownManager<E> implements ICooldownManager<E> {
    private final @NotNull CooldownManager<E> delegate = new CooldownManager<>();

    private final long cooldown;

    /**
     * Instantiates a new Fixed cooldown manager.
     *
     * @param cooldown the duration of the cooldown in milliseconds
     */
    public FixedCooldownManager(final @Range(from = 1, to = Long.MAX_VALUE) long cooldown) {
        if (cooldown <= 0) throw new IllegalArgumentException("cooldown must be positive");
        this.cooldown = cooldown;
    }

    /**
     * Instantiates a new Fixed cooldown manager.
     *
     * @param cooldown the duration of the cooldown
     */
    public FixedCooldownManager(final @NotNull Duration cooldown) {
        this(cooldown.toMillis());
    }

    /**
     * Puts the entity on cooldown.
     *
     * @param entity the entity
     * @return this object (for method chaining)
     */
    public @NotNull FixedCooldownManager<E> put(final @NotNull E entity) {
        delegate.put(entity, cooldown);
        return this;
    }

    @Override
    public @NotNull FixedCooldownManager<E> remove(final @NotNull E entity) {
        delegate.remove(entity);
        return this;
    }

    @Override
    public boolean isOnCooldown(final @NotNull E entity) {
        return delegate.isOnCooldown(entity);
    }

    @Override
    public long getRemaining(final @NotNull E entity) {
        return delegate.getRemaining(entity);
    }

}
