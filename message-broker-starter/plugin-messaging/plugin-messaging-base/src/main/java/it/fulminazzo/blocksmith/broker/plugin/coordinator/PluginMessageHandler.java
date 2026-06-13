package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * Functional interface for handling incoming plugin messages.
 *
 * @see PluginMessageChannelCoordinator
 */
@FunctionalInterface
public interface PluginMessageHandler {

    /**
     * Handles the message.
     *
     * @param message the message
     */
    void handle(final @NotNull String message);

}
