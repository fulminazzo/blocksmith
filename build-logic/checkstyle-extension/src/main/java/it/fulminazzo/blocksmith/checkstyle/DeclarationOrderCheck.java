package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checkstyle.validator.OrderValidator;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.DeclarationCriterion;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates the order of the elements in a class.
 * Elements declaration must respect the following rules:
 * <ul>
 *     <li><b>fields</b>;</li>
 *     <li><b>constructors</b>;</li>
 *     <li><b>methods</b>;</li>
 *     <li><b>nested types</b> (interfaces, enums, classes...);</li>
 * </ul>
 */
public final class DeclarationOrderCheck extends ValidatorCheck {

    /**
     * Instantiates a new Declaration order check.
     */
    public DeclarationOrderCheck() {
        super(new OrderValidator(DeclarationCriterion.values()));
    }

    @Override
    protected @NotNull List<Integer> getTokens() {
        return Arrays.stream(DeclarationCriterion.values())
                .map(DeclarationCriterion::getTypes)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void visitToken(final @NotNull DetailAST ast) {
        if (isTopLevel(ast)) return;
        if (!isSpecialScopeChangeToken(ast)
                && ast.getType() != TokenTypes.VARIABLE_DEF
                || CheckUtils.isField(ast))
            visitTokenImpl(ast);
        if (isScopeChanged(ast)) getValidator().enterScope();
    }

}
