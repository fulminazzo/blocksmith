package it.fulminazzo.blocksmith.checkstyle.validator;

import it.fulminazzo.blocksmith.ProjectInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown by {@link NodeOrderValidator}.
 *
 * @see NodeOrderValidator
 */
public final class ValidationException extends Exception {
    private static final long serialVersionUID = -7493503922158653249L;

    /**
     * Instantiates a new Validation exception.
     *
     * @param messageCode the message code in the properties file
     */
    ValidationException(final @NotNull String messageCode) {
        super(
                ProjectInfo.GROUP + "."
                        + ProjectInfo.PROJECT_NAME + "."
                        + ProjectInfo.MODULE_NAME.replace("-extension", "") + "."
                        + messageCode
        );
    }

}
