package it.fulminazzo.blocksmith.broker.tcp.peer;

import org.jetbrains.annotations.NotNull;

/**
 * Handler for incoming messages.
 */
@FunctionalInterface
public interface MessageHandler {

    /**
     * Handles the message.
     *
     * @param message the message
     */
    void handle(final @NotNull String message);

}
