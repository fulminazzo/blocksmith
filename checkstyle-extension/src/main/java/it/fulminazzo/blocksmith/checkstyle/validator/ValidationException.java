package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.ProjectInfo;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown by {@link NodeOrderValidator}.
 *
 * @see NodeOrderValidator
 */
public final class ValidationException extends Exception {
    private static final long serialVersionUID = -7493503922158653249L;

    @Getter
    private final @NotNull DetailAST node;

    /**
     * Instantiates a new Validation exception.
     *
     * @param node        the node that triggered the exception
     * @param messageCode the message code in the properties file
     */
    ValidationException(final @NotNull DetailAST node, final @NotNull String messageCode) {
        super(
                ProjectInfo.GROUP + "."
                        + ProjectInfo.PROJECT_NAME + "."
                        + ProjectInfo.MODULE_NAME.replace("-extension", "") + "."
                        + messageCode
        );
        this.node = node;
    }

}
