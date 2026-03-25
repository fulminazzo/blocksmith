package it.fulminazzo.blocksmith.cooldown;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager to handle general cooldowns for entities.
 *
 * @param <T> the type of the entity
 */
@RequiredArgsConstructor
public final class CooldownManager<T> {
    private final @NotNull Duration cooldown;

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
        return lastUsage.getOrDefault(entity, 0L) > System.currentTimeMillis();
    }

}
