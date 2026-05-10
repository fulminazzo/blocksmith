package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checkstyle.validator.OrderValidator;
import it.fulminazzo.blocksmith.checkstyle.validator.OverloadValidator;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Validates the order of constructors in a class.
 * Constructors declaration must respect the following rules:
 * <ul>
 *     <li><b>visibility</b> ordering of the fields must be {@code public}, {@code protected},
 *     package-private and {@code private};</li>
 *     <li><b>ordered overloads</b>: overloads should be grouped and sorted by their parameter count.
 *     Overloads with the same number of parameters must have primitive parameters before wrappers.</li>
 * </ul>
 */
public final class ConstructorsOrderCheck extends ValidatorCheck {

    /**
     * Instantiates a new Constructors order check.
     */
    public ConstructorsOrderCheck() {
        super(new OrderValidator(VisibilityCriterion.values()).then(new OverloadValidator()));
    }

    @Override
    protected @NotNull List<Integer> getTokens() {
        return List.of(TokenTypes.CTOR_DEF);
    }

}
