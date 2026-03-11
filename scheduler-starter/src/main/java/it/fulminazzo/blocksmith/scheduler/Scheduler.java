package it.fulminazzo.blocksmith.scheduler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A singleton to schedule tasks for later or repeated execution.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Scheduler {
    private static final @NotNull Collection<TaskFactory> factories = ServiceLoader.load(TaskFactory.class).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toSet());

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
    public static <T> @NotNull CompletableFuture<Task> runAsyncThen(final @NotNull Object owner,
                                                                    final @NotNull CompletableFuture<T> function,
                                                                    final @NotNull Consumer<T> then) {
        return getFactory(owner.getClass()).runAsyncThen(owner, function, then);
    }

    /**
     * Schedules a new task.
     *
     * @param owner    the owner of the task
     * @param function the function to run
     * @return the task builder (to specify how the task should be run)
     */
    public static @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        return getFactory(owner.getClass()).schedule(owner, function);
    }

    /**
     * Gets the most appropriate factory from the given type
     *
     * @param ownerType the owner type
     * @return the factory
     */
    static @NotNull TaskFactory getFactory(final @NotNull Class<?> ownerType) {
        for (TaskFactory factory : factories)
            if (factory.supportsOwner(ownerType))
                return factory;
        throw new IllegalArgumentException("No TaskFactory found for owner type: " + ownerType.getCanonicalName());
    }

}
