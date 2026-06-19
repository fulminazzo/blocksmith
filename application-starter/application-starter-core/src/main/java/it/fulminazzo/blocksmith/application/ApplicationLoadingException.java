package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown when an {@link Application} instance fails to load.
 *
 * @see Application
 * @see ApplicationLoader
 */
final class ApplicationLoadingException extends RuntimeException {
    private static final long serialVersionUID = 2794354207950913415L;

    /**
     * Instantiates a new Application loading exception.
     *
     * @param format the format of the message
     * @param args   the arguments to format
     */
    public ApplicationLoadingException(final @NotNull String format, final @Nullable Object @NotNull ... args) {
        super(String.format(format, args));
    }

}
