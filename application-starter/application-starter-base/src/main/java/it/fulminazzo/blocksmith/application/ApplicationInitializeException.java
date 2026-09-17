package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown during a failed initialization of the application.
 */
public final class ApplicationInitializeException extends Exception {
    private static final long serialVersionUID = 6819735949224600789L;

    /**
     * Instantiates a new Application initialize exception.
     *
     * @param message the message
     */
    public ApplicationInitializeException(final @NotNull String message) {
        super(message);
    }

    /**
     * Instantiates a new Application initialize exception.
     *
     * @param cause the cause
     */
    public ApplicationInitializeException(final @NotNull Throwable cause) {
        super(cause);
    }

}
