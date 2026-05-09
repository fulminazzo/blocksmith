package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checkstyle.validator.ValidationException;
import it.fulminazzo.blocksmith.checkstyle.validator.Validator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract implementation of {@link AbstractCheck} with common logic for validating with a {@link Validator}.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class ValidatorCheck extends AbstractCheck {
    @Getter(AccessLevel.PROTECTED)
    private final @NotNull Validator validator;

    /**
     * Executes the actual logic when visiting a node.
     *
     * @param node the node
     */
    protected void visitTokenImpl(final @NotNull DetailAST node) {
        validate(node);
    }

    /**
     * Validates the given node.
     * In case of {@link ValidationException}, the error message will be logged.
     *
     * @param node the node to validate
     */
    protected void validate(final @NotNull DetailAST node) {
        try {
            validator.validateNode(node);
        } catch (ValidationException e) {
            log(e.getNode(), e.getMessage());
        }
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(final @NotNull DetailAST ast) {
        visitTokenImpl(ast);
    }

}
