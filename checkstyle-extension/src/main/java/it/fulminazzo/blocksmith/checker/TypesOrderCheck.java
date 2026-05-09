package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checker.validator.StaticValidator;
import it.fulminazzo.blocksmith.checker.validator.TypeValidator;
import it.fulminazzo.blocksmith.checker.validator.VisibilityValidator;
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
public final class TypesOrderCheck extends OrderCheck {

    /**
     * Instantiates a new Types order check.
     */
    public TypesOrderCheck() {
        super(new NodeValidator(
                TypeValidator.values(),
                StaticValidator.values(),
                VisibilityValidator.values()
        ));
    }

    @Override
    protected @NotNull List<Integer> getTokens() {
        return Arrays.stream(TypeValidator.values()).map(TypeValidator::getType).collect(Collectors.toList());
    }

    @Override
    public void visitToken(final @NotNull DetailAST ast) {
        visitTokenImpl(ast);
        if (isScopeChanged(ast)) validator.enterScope();
    }

}
