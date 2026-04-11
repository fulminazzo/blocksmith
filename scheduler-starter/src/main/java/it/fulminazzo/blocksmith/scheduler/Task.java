package it.fulminazzo.blocksmith.scheduler;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a chunk of code to be executed asynchronously, later on or repeatedly.
 */
public interface Task {

    /**
     * Cancels the task (if running or scheduled to be run).
     */
    void cancel();

    /**
     * Checks if the task has been canceled forcibly before completing execution.
     *
     * @return <code>true</code> if it has
     */
    boolean isCancelled();

    /**
     * Gets the owner of this task.
     *
     * @return the owner
     */
    @NotNull Object getOwner();

    /**
     * Checks if the task is asynchronous.
     *
     * @return <code>true</code> if it is
     */
    boolean isAsync();

}
