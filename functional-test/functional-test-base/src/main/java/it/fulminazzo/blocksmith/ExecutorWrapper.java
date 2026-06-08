package it.fulminazzo.blocksmith;

import org.jetbrains.annotations.NotNull;

/**
 * A wrapper for the actual executor of a command.
 */
public interface ExecutorWrapper {

    /**
     * Sends a message to the executor.
     *
     * @param message the message
     */
    void sendMessage(final @NotNull String message);

    /**
     * Gets the name of the executor.
     *
     * @return the name
     */
    @NotNull String getName();

}
