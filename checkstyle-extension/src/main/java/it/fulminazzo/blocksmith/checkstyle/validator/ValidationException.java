package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.ProjectInfo;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown by {@link OrderValidator}.
 *
 * @see OrderValidator
 */
public final class ValidationException extends Exception {
    private static final long serialVersionUID = -7493503922158653249L;

    @Getter
    private final @NotNull DetailAST node;
    private final @NotNull List<Object> arguments = new ArrayList<>();

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

    /**
     * Gets the arguments to format.
     *
     * @return the arguments
     */
    public @Nullable Object @NotNull [] getArguments() {
        return arguments.toArray();
    }

    /**
     * Adds an argument to format.
     *
     * @param argument the argument
     * @return this object (for method chaining)
     */
    public @NotNull ValidationException addArgument(final @Nullable Object argument) {
        arguments.add(argument);
        return this;
    }

}
