package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown when a {@link Application} instance is invalid.
 *
 * @see Application
 * @see ApplicationLoader
 */
final class InvalidApplicationException extends RuntimeException {
    private static final long serialVersionUID = 2794354207950913415L;

    /**
     * Instantiates a new Invalid application exception.
     *
     * @param format the format of the message
     * @param args   the arguments to format
     */
    public InvalidApplicationException(final @NotNull String format, final @Nullable Object @NotNull ... args) {
        super(String.format(format, args));
    }

}
