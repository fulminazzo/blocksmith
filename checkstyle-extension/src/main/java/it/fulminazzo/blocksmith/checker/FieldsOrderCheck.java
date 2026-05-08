package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checker.validator.MutabilityValidator;
import it.fulminazzo.blocksmith.checker.validator.StaticValidator;
import it.fulminazzo.blocksmith.checker.validator.VisibilityValidator;
import org.jetbrains.annotations.NotNull;

/**
 * Validates the order of fields in a class.
 * Fields declaration must respect the following rules:
 * <ul>
 *     <li>{@code static} fields must go <b>before</b> <b>instance</b> fields;</li>
 *     <li>{@code final} fields must be declared <b>before</b> <b>mutable</b> fields;</li>
 *     <li><b>visibility</b> ordering of the fields must be {@code public}, {@code protected},
 *     package-private and {@code private}.</li>
 * </ul>
 */
public final class FieldsOrderCheck extends AbstractCheck {
    private final @NotNull NodeValidator validator = new NodeValidator(
            StaticValidator.values(),
            MutabilityValidator.values(),
            VisibilityValidator.values()
    );

    @Override
    public int[] getDefaultTokens() {
        return new int[]{
                TokenTypes.VARIABLE_DEF,
                TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF,
                TokenTypes.ENUM_DEF, TokenTypes.RECORD_DEF
        };
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
        if (ast.getType() != TokenTypes.VARIABLE_DEF) {
            // New type declaration
            validator.enterScope();
            return;
        }
        if (ast.getParent().getType() != TokenTypes.OBJBLOCK) return;

        try {
            validator.validateNode(ast);
        } catch (ValidationException e) {
            log(ast, e.getMessage());
        }
    }

    @Override
    public void leaveToken(final @NotNull DetailAST ast) {
        if (ast.getType() != TokenTypes.VARIABLE_DEF)
            validator.exitScope();
    }

}
