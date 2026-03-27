package it.fulminazzo.blocksmith.action;

import it.fulminazzo.blocksmith.cooldown.CooldownManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A special manager to store actions to be run later.
 *
 * @param <T> the type of the entity executing the action
 */
public final class PendingActionManager<T> {
    private final @NotNull Map<T, PendingAction> pendingActions = new ConcurrentHashMap<>();
    private final @NotNull CooldownManager<T> cooldownManager = new CooldownManager<>();

    /**
     * Registers a pending action for the given entity.
     *
     * @param entity        the entity
     * @param timeout       the timeout upon which the entity must execute the action (or it will be considered invalid)
     * @param pendingAction the pending action
     */
    public void register(final @NotNull T entity,
                         final @NotNull Duration timeout,
                         final @NotNull PendingAction pendingAction) {
        pendingActions.put(entity, pendingAction);
        cooldownManager.putOnCooldown(entity, timeout);
    }

    /**
     * Executes the pending action for the given entity.
     *
     * @param entity the entity
     * @return {@link Result#SUCCESS} if the action was executed,
     * {@link Result#EXPIRED} if the action was executed too late,
     * {@link Result#NOT_FOUND} if the entity has no pending action
     */
    public @NotNull Result execute(final @NotNull T entity) {
        PendingAction action = pendingActions.remove(entity);
        if (action == null) return Result.NOT_FOUND;
        if (cooldownManager.isOnCooldown(entity)) {
            cooldownManager.removeFromCooldown(entity);
            action.run();
            return Result.SUCCESS;
        } else return Result.EXPIRED;
    }

    /**
     * Represents the result of an action execution.
     */
    public enum Result {
        /**
         * The action was executed successfully.
         */
        SUCCESS,
        /**
         * The action was executed too late.
         */
        EXPIRED,
        /**
         * No action to execute was found.
         */
        NOT_FOUND
    }

}
