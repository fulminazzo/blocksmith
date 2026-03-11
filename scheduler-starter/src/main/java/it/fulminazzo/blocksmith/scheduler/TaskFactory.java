package it.fulminazzo.blocksmith.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A factory to build and instantiate {@link Task} objects.
 */
public interface TaskFactory {

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
     * @return <code>true</code> if it does
     */
    boolean supportsOwner(final @NotNull Class<?> ownerType);

}
