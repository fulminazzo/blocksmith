package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checkstyle.validator.OrderValidator;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.StaticCriterion;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.TypeCriterion;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates the order of nested types in a class.
 * Types declaration must respect the following rules:
 * <ul>
 *     <li><b>type</b> ordering of the types must be {@code interface}, {@code enum},
 *     {@code record} and {@code class};</li>
 *     <li>{@code static} types must go <b>before</b> <b>instance</b> types;</li>
 *     <li><b>visibility</b> ordering of the types must be {@code public}, {@code protected},
 *     package-private and {@code private}.</li>
 * </ul>
 */
public final class TypesOrderCheck extends ValidatorCheck {

    /**
     * Instantiates a new Types order check.
     */
    public TypesOrderCheck() {
        super(new OrderValidator(
                TypeCriterion.values(),
                StaticCriterion.values(),
                VisibilityCriterion.values()
        ));
    }

    @Override
    protected @NotNull List<Integer> getTokens() {
        return Arrays.stream(TypeCriterion.values()).map(TypeCriterion::getType).collect(Collectors.toList());
    }

    @Override
    public void visitToken(final @NotNull DetailAST ast) {
        if (isTopLevel(ast)) return;
        if (ast.getType() != TokenTypes.SLIST) visitTokenImpl(ast);
        if (isScopeChanged(ast)) getValidator().enterScope();
    }

}
