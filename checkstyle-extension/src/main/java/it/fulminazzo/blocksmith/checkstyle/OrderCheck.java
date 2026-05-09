package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checkstyle.validator.OrderValidator;
import it.fulminazzo.blocksmith.checkstyle.validator.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Abstract implementation of {@link ValidatorCheck} with common logic for validating the correct ordering of nodes.
 * <br>
 * When entering a new type declaration, a new scope for the {@link OrderValidator} will be created.
 */
abstract class OrderCheck extends ValidatorCheck {
    private static final @NotNull List<Integer> SCOPE_CHANGE_TOKENS = List.of(
            TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.ENUM_DEF, TokenTypes.RECORD_DEF
    );

    /**
     * Instantiates a new Order check.
     *
     * @param validator the validator
     */
    protected OrderCheck(final @NotNull OrderValidator validator) {
        super(validator);
    }

    /**
     * Gets the tokens that this check will validate.
     *
     * @return the tokens
     */
    protected abstract @NotNull List<Integer> getTokens();

    @Override
    protected @NotNull OrderValidator getValidator() {
        return (OrderValidator) super.getValidator();
    }

    @Override
    public int[] getDefaultTokens() {
        return Stream.concat(
                getTokens().stream(),
                SCOPE_CHANGE_TOKENS.stream()
        ).mapToInt(i -> i).distinct().toArray();
    }

    @Override
    public void visitToken(final @NotNull DetailAST ast) {
        if (isScopeChanged(ast)) getValidator().enterScope();
        else visitTokenImpl(ast);
    }

    @Override
    public void leaveToken(final @NotNull DetailAST ast) {
        try {
            if (isScopeChanged(ast)) getValidator().exitScope();
        } catch (ValidationException e) {
            log(e.getNode(), e.getMessage());
        }
    }

    /**
     * Checks if the scope changed according to the {@link #SCOPE_CHANGE_TOKENS}.
     *
     * @param node the node to check
     * @return {@code true} if the scope changed, {@code false} otherwise
     */
    protected static boolean isScopeChanged(final @NotNull DetailAST node) {
        return SCOPE_CHANGE_TOKENS.contains(node.getType());
    }

}
