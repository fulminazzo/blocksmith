package it.fulminazzo.blocksmith.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A factory to build and instantiate {@link Task} objects.
 */
public interface TaskFactory {

    /**
     * Executes the given future asynchronously.
     * Then, it will pass the result of the computation to the
     * given consumer in a synchronous context.
     *
     * @param <T>      the type of the returned data
     * @param owner    the owner of the task
     * @param function the future to complete before calling the function
     * @param then     the function to run
     * @return a future with the task responsible for executing the function
     */
    default <T> @NotNull CompletableFuture<Task> runAsyncThen(final @NotNull Object owner,
                                                              final @NotNull CompletableFuture<T> function,
                                                              final @NotNull Consumer<T> then) {
        return function.thenApply(r -> schedule(owner, t -> then.accept(r)).run())
                .exceptionally(t -> {
                    Throwable cause = t.getCause();
                    if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                    else if (cause instanceof Error) throw (Error) cause;
                    else throw new RuntimeException(cause);
                });
    }

    /**
     * Schedules a new task.
     *
     * @param owner    the owner of the task
     * @param function the function to run
     * @return the task builder (to specify how the task should be run)
     */
    @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function);

    /**
     * Checks if the current factory supports the provided owner type.
     *
     * @param ownerType the owner type
     * @return {@code true} if it does
     */
    boolean supportsOwner(final @NotNull Class<?> ownerType);

}
