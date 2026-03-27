package it.fulminazzo.blocksmith.cooldown;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager to handle general cooldowns for entities.
 *
 * @param <T> the type of the entity
 */
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class CooldownManager<T> {
    @Getter
    private final @NotNull Duration cooldown;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final @NotNull Map<T, Long> lastUsage = new ConcurrentHashMap<>();

    /**
     * Puts the given entity on cooldown.
     *
     * @param entity the entity
     */
    public void putOnCooldown(final @NotNull T entity) {
        lastUsage.put(entity, System.currentTimeMillis() + cooldown.toMillis());
    }

    /**
     * Checks if the given entity is on cooldown.
     *
     * @param entity the entity
     * @return <code>true</code> if it is
     */
    public boolean isOnCooldown(final @NotNull T entity) {
        Long last = lastUsage.get(entity);
        if (last == null) return false;
        if (last > getNow()) return true;
        lastUsage.remove(entity);
        return false;
    }

    /**
     * Gets the remaining time before the cooldown is exhausted.
     *
     * @param entity the entity
     * @return the time in milliseconds
     */
    public long getRemainingCooldown(final @NotNull T entity) {
        if (isOnCooldown(entity)) return lastUsage.get(entity) - getNow();
        else throw new IllegalArgumentException(String.format("Entity '%s' is not on cooldown", entity));
    }

    private static long getNow() {
        return System.currentTimeMillis();
    }

}
