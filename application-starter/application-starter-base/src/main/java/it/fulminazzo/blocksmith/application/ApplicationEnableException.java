package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown when an {@link Application} instance fails to enable.
 *
 * @see Application
 */
public final class ApplicationEnableException extends Exception {
    private static final long serialVersionUID = 2494223724076953851L;

    /**
     * Instantiates a new Application enable exception.
     *
     * @param format the format of the message
     * @param args   the arguments to format
     */
    public ApplicationEnableException(final @NotNull String format, final @Nullable Object @NotNull ... args) {
        super(String.format(format, args));
    }

    /**
     * Instantiates a new Application enable exception.
     *
     * @param cause  the cause that generated the exception
     * @param format the format of the message
     * @param args   the arguments to format
     */
    public ApplicationEnableException(
            final @NotNull Throwable cause,
            final @NotNull String format,
            final @Nullable Object @NotNull ... args
    ) {
        super(String.format(format, args), cause);
    }

}
