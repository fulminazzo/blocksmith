package it.fulminazzo.blocksmith.data.mapper;

import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown by {@link Mapper} implementations.
 */
public final class MapperException extends RuntimeException {

    /**
     * Instantiates a new Mapper exception.
     *
     * @param message the message
     */
    public MapperException(final @NotNull String message) {
        super(message);
    }

    /**
     * Instantiates a new Mapper exception.
     *
     * @param cause the cause
     */
    public MapperException(final @NotNull Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
