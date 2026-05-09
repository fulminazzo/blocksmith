package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checkstyle.validator.OrderValidator;
import it.fulminazzo.blocksmith.checkstyle.validator.OverloadValidator;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.MethodNameCriterion;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.MethodTraitCriterion;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Validates the order of methods in a class.
 * Methods declaration must respect the following rules:
 * <ul>
 *     <li>{@code abstract} methods must go <b>before</b> concrete implementations,
 *     {@code @Override} annotated methods must go <b>after</b> type own methods,
 *     {@code static} methods must go <b>after</b> overrides;</li>
 *     <li><b>visibility</b> ordering of the methods must be {@code public}, {@code protected},
 *     package-private and {@code private};</li>
 *     <li><b>ordered overloads</b>: overloads should be grouped and sorted by their parameter count.
 *     Overloads with the same number of parameters must have primitive parameters before wrappers;</li>
 *     <li><b>name</b> ordering: {@code equals}, {@code hashCode} and {@code toString} should be last.
 *     Then, before them any <b>getter</b> or <b>setter</b>.</li>
 * </ul>
 */
public final class MethodsOrderCheck extends ValidatorCheck {

    /**
     * Instantiates a new Constructors order check.
     */
    public MethodsOrderCheck() {
        super(new OrderValidator(
                MethodTraitCriterion.values(),
                VisibilityCriterion.values()
        ).then(new OverloadValidator().then(new OrderValidator(MethodNameCriterion.values()))));
    }

    @Override
    protected @NotNull List<Integer> getTokens() {
        return List.of(TokenTypes.METHOD_DEF);
    }

}
