package it.fulminazzo.blocksmith.structure.cooldown;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;

/**
 * Keeps track of the same cooldown for general entities.
 *
 * @param <E> the type of the entity
 */
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public final class FixedCooldownManager<E> implements ICooldownManager<E> {
    private final @NotNull CooldownManager<E> delegate = new CooldownManager<>();

    private final long cooldown;

    /**
     * Instantiates a new Fixed cooldown manager.
     *
     * @param cooldown the duration of the cooldown
     */
    public FixedCooldownManager(final @NotNull Duration cooldown) {
        this(cooldown.toMillis());
    }

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
    public boolean isOnCooldown(final @NotNull E entity) {
        return delegate.isOnCooldown(entity);
    }

    @Override
    public long getRemaining(final @NotNull E entity) {
        return delegate.getRemaining(entity);
    }

    @Override
    public @NotNull FixedCooldownManager<E> remove(final @NotNull E entity) {
        delegate.remove(entity);
        return this;
    }

}
