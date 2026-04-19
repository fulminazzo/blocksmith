package it.fulminazzo.blocksmith.structure.task;

import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * A manager to store tasks to be run later.
 *
 * @param <E> the type of the owner of the task
 */
@EqualsAndHashCode
public final class PendingTaskManager<E> {
    private final @NotNull ExpiringMap<E, Runnable> tasks = ExpiringMap.passive();

    /**
     * Registers a new pending task with the given entity as owner.
     *
     * @param entity  the entity
     * @param timeout the timeout upon which the task will be considered as expired
     * @param task    the task
     */
    public void register(final @NotNull E entity, final @NotNull Duration timeout, final @NotNull Runnable task) {
        register(entity, timeout.toMillis(), task);
    }

    /**
     * Registers a new pending task with the given entity as owner.
     *
     * @param entity  the entity
     * @param timeout the timeout in milliseconds upon which the task will be considered as expired
     * @param task    the task
     */
    public void register(final @NotNull E entity,
                         final @Range(from = 1, to = Long.MAX_VALUE) long timeout,
                         final @NotNull Runnable task) {
        tasks.put(entity, task, timeout);
    }

    /**
     * Executes the pending task for the given entity.
     *
     * @param entity the owner of the task
     * @return {@link Result#SUCCESS} if the task was executed,
     * {@link Result#EXPIRED} if the task was executed too late,
     * {@link Result#NOT_FOUND} if the entity has no pending task
     */
    public @NotNull Result execute(final @NotNull E entity) {
        return fetchTask(entity, Runnable::run);
    }

    /**
     * Cancels the pending task for the given entity.
     *
     * @param entity the owner of the task
     * @return {@link Result#SUCCESS} if the task was successfully removed,
     * {@link Result#EXPIRED} if the task was removed too late,
     * {@link Result#NOT_FOUND} if the entity has no pending task
     */
    public @NotNull Result cancel(final @NotNull E entity) {
        return fetchTask(entity, r -> {
        });
    }

    /**
     * Attempts to fetch and removes a pending task for the given entity.
     *
     * @param entity the owner of the task
     * @param then   the action to do with the task (if found)
     * @return {@link Result#SUCCESS} if the task was successfully removed,
     * {@link Result#EXPIRED} if the task was removed too late,
     * {@link Result#NOT_FOUND} if the entity has no pending task
     */
    @NotNull Result fetchTask(final @NotNull E entity, final Consumer<@NotNull Runnable> then) {
        Duration ttl = tasks.getTtl(entity);
        if (ttl == null) return Result.NOT_FOUND;
        Runnable task = tasks.remove(entity);
        if (ttl.toMillis() <= 0) return Result.EXPIRED;
        else {
            then.accept(task);
            return Result.SUCCESS;
        }
    }

    /**
     * Identifies the result of a pending task fetch request.
     */
    public enum Result {
        /**
         * The task associated with the entity was fetched correctly.
         */
        SUCCESS,
        /**
         * The task associated with the entity had already expired when requested.
         */
        EXPIRED,
        /**
         * The task associated with the entity was not found.
         */
        NOT_FOUND
    }

}
