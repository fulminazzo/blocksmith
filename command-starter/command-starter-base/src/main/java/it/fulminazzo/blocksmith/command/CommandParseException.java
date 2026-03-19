package it.fulminazzo.blocksmith.command;

import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown by {@link CommandNodeBuilder} when the parsing of a command fails.
 */
public final class CommandParseException extends RuntimeException {

    /**
     * Instantiates a new Command parse exception.
     *
     * @param message the message
     */
    public CommandParseException(final @NotNull String message) {
        super(message);
    }

}
