package it.fulminazzo.blocksmith.checker;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown by {@link NodeValidator}.
 *
 * @see NodeValidator
 */
public final class ValidationException extends Exception {
    private static final long serialVersionUID = -7493503922158653249L;

    /**
     * Instantiates a new Validation exception.
     *
     * @param messageCode the message code in the properties file
     */
    ValidationException(final @NotNull String messageCode) {
        super(ValidationException.class.getPackageName() + "." + messageCode);
    }

}
