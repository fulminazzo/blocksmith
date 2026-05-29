package it.fulminazzo.blocksmith.broker.tcp.peer_rework;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Represents a general object that can log messages.
 * Provides a common format for log messages.
 */
@RequiredArgsConstructor
public abstract class Loggable {
    protected final @NotNull Logger logger;

    /**
     * Formats the message with the common format.
     *
     * @param message the message to be logged
     * @return the formatted message
     */
    protected abstract @NotNull String formatLog(final @NotNull String message);

}
