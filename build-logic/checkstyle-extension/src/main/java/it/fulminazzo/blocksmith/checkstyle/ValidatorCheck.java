package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checkstyle.validator.CompositeValidationException;
import it.fulminazzo.blocksmith.checkstyle.validator.ValidationException;
import it.fulminazzo.blocksmith.checkstyle.validator.Validator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Abstract implementation of {@link AbstractCheck} with common logic for validating with a {@link Validator}.
 * <br>
 * When entering a new type declaration, a new scope for the {@link Validator} will be created.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class ValidatorCheck extends AbstractCheck {
    private static final @NotNull List<Integer> scopeChangeTokens = List.of(
            TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.ENUM_DEF, TokenTypes.RECORD_DEF,
            TokenTypes.SLIST
    );

    @Getter(AccessLevel.PROTECTED)
    private final @NotNull Validator validator;

    /**
     * Gets the tokens that this check will validate.
     *
     * @return the tokens
     */
    protected abstract @NotNull List<Integer> getTokens();

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
            log(e.getNode(), e.getMessage(), e.getArguments());
        }
    }

    /**
     * Checks if the scope changed according to the {@link #scopeChangeTokens}.
     *
     * @param node the node to check
     * @return {@code true} if the scope changed, {@code false} otherwise
     */
    protected boolean isScopeChanged(final @NotNull DetailAST node) {
        return scopeChangeTokens.contains(node.getType());
    }

    @Override
    public int[] getDefaultTokens() {
        return Stream.concat(
                getTokens().stream(),
                scopeChangeTokens.stream()
        ).mapToInt(i -> i).distinct().toArray();
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
        if (isTopLevel(ast)) return;
        if (isScopeChanged(ast)) getValidator().enterScope();
        else visitTokenImpl(ast);
    }

    @Override
    public void beginTree(final DetailAST rootAST) {
        validator.enterScope();
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        try {
            validator.exitScope();
        } catch (CompositeValidationException c) {
            handle(c);
        }
    }

    @Override
    public void leaveToken(final @NotNull DetailAST ast) {
        try {
            if (isScopeChanged(ast)) getValidator().exitScope();
        } catch (CompositeValidationException c) {
            handle(c);
        }
    }

    private void handle(final CompositeValidationException exception) {
        exception.getExceptions().forEach(e -> log(e.getNode(), e.getMessage(), e.getArguments()));
    }

    /**
     * Checks if the given node is at the top level.
     *
     * @param node the node to check
     * @return {@code true} if it is
     */
    protected static boolean isTopLevel(final @NotNull DetailAST node) {
        return node.getParent() == null || node.getParent().getType() == TokenTypes.COMPILATION_UNIT;
    }

}
