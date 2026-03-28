package it.fulminazzo.blocksmith.cooldown;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A manager to handle general cooldowns for entities.
 * Every entity will have the same cooldown.
 *
 * @param <T> the type of the entity
 */
@EqualsAndHashCode(callSuper = false)
@ToString
@RequiredArgsConstructor
public final class StaticCooldownManager<T> extends CooldownManager<T> {
    @Getter
    private final @NotNull Duration cooldown;

    /**
     * Puts the given entity on cooldown.
     *
     * @param entity the entity
     */
    public void putOnCooldown(final @NotNull T entity) {
        putOnCooldown(entity, cooldown);
    }

}
