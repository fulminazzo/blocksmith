package it.fulminazzo.blocksmith.structure.task;

import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * A manager to store tasks to be run later.
 * This is useful when a task needs to be executed at a later time,
 * due to timeout, confirmation from the user or any other reason.
 * <br>
 * Usage example:
 * <pre>{@code
 * PendingTaskManager<String> manager = new PendingTaskManager<>();
 * String owner = ...; // the owner of the task
 * manager.register(
 *     owner,
 *     Duration.ofSeconds(10), // set to Long.MAX_VALUE to never expire
 *     () -> System.out.println("Hello, " + owner)
 * );
 * // later
 * Result result = manager.execute(owner);
 * }</pre>
 * The {@link Result} will be:
 * <ul>
 *     <li>{@link Result#SUCCESS} if the task was executed in time;</li>
 *     <li>{@link Result#EXPIRED} if the execute method was called too late,
 *     resulting in the task being expired and <b>not</b> executed;</li>
 *     <li>{@link Result#NOT_FOUND} if the owner has no pending task.</li>
 * </ul>
 * It is also possible to cancel the task with {@link #cancel(Object)}.
 *
 * @param <E> the type of the owner of the task
 * @see Result
 * @see ExpiringMap
 */
@EqualsAndHashCode
public final class PendingTaskManager<E> {
    private final @NotNull ExpiringMap<E, Runnable> tasks = ExpiringMap.passive();

    /**
     * Executes the pending task for the given owner.
     *
     * @param owner the owner of the task
     * @return {@link Result#SUCCESS} if the task was executed,
     *         {@link Result#EXPIRED} if the task was executed too late,
     *         {@link Result#NOT_FOUND} if the owner has no pending task
     */
    public @NotNull Result execute(final @NotNull E owner) {
        return fetchTask(owner, Runnable::run);
    }

    /**
     * Cancels the pending task for the given owner.
     *
     * @param owner the owner of the task
     * @return {@link Result#SUCCESS} if the task was successfully removed,
     *         {@link Result#EXPIRED} if the task was removed too late,
     *         {@link Result#NOT_FOUND} if the owner has no pending task
     */
    public @NotNull Result cancel(final @NotNull E owner) {
        return fetchTask(owner, r -> {
        });
    }

    /**
     * Registers a new pending task with the given owner as owner.
     *
     * @param owner   the owner
     * @param timeout the timeout in milliseconds upon which the task will be considered as expired
     * @param task    the task
     */
    public void register(
            final @NotNull E owner,
            final @Range(from = 1, to = Long.MAX_VALUE) long timeout,
            final @NotNull Runnable task
    ) {
        tasks.put(owner, task, timeout);
    }

    /**
     * Registers a new pending task with the given owner as owner.
     *
     * @param owner   the owner
     * @param timeout the timeout upon which the task will be considered as expired
     * @param task    the task
     */
    public void register(final @NotNull E owner, final @NotNull Duration timeout, final @NotNull Runnable task) {
        register(owner, timeout.toMillis(), task);
    }

    /**
     * Attempts to fetch and removes a pending task for the given owner.
     *
     * @param owner the owner of the task
     * @param then  the action to do with the task (if found)
     * @return {@link Result#SUCCESS} if the task was successfully removed,
     *         {@link Result#EXPIRED} if the task was removed too late,
     *         {@link Result#NOT_FOUND} if the owner has no pending task
     */
    @NotNull Result fetchTask(final @NotNull E owner, final Consumer<@NotNull Runnable> then) {
        Duration ttl = tasks.getTtl(owner);
        if (ttl == null) return Result.NOT_FOUND;
        Runnable task = tasks.remove(owner);
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
         * The task associated with the owner was fetched correctly.
         */
        SUCCESS,
        /**
         * The task associated with the owner had already expired when requested.
         */
        EXPIRED,
        /**
         * The task associated with the owner was not found.
         */
        NOT_FOUND
    }

}
