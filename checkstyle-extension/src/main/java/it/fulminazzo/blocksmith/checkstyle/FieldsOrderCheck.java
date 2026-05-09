package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.MutabilityCriterion;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.StaticCriterion;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion;
import it.fulminazzo.blocksmith.checkstyle.validator.NodeOrderValidator;
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
                StaticCriterion.values(),
                MutabilityCriterion.values(),
                VisibilityCriterion.values()
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
