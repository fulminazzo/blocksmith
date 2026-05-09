package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checker.validator.MutabilityValidator;
import it.fulminazzo.blocksmith.checker.validator.StaticValidator;
import it.fulminazzo.blocksmith.checker.validator.VisibilityValidator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
public final class FieldsOrderCheck extends OrderCheck {

    /**
     * Instantiates a new Fields order check.
     */
    public FieldsOrderCheck() {
        super(new NodeOrderValidator(
                StaticValidator.values(),
                MutabilityValidator.values(),
                VisibilityValidator.values()
        ));
    }

    @Override
    protected @NotNull List<Integer> getTokens() {
        return List.of(TokenTypes.VARIABLE_DEF);
    }

    @Override
    protected void visitTokenImpl(final @NotNull DetailAST ast) {
        if (ast.getParent().getType() == TokenTypes.OBJBLOCK) validate(ast);
    }

}
