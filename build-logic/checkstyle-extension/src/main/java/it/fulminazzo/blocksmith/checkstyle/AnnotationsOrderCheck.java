package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import it.fulminazzo.blocksmith.checkstyle.validator.OrderValidator;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.AnnotationCriterion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Validates the order of annotations.
 * Check {@link AnnotationCriterion} to see the necessary order.
 */
public final class AnnotationsOrderCheck extends ValidatorCheck {

    /**
     * Instantiates a new Annotations order check.
     */
    public AnnotationsOrderCheck() {
        super(new OrderValidator(AnnotationCriterion.values()));
    }

    @Override
    protected @NotNull List<Integer> getTokens() {
        return List.of(
                TokenTypes.ANNOTATION,
                TokenTypes.VARIABLE_DEF,
                TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF,
                TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.ENUM_DEF, TokenTypes.RECORD_DEF
        );
    }

    @Override
    protected boolean isScopeChanged(final @NotNull DetailAST node) {
        return node.getType() != TokenTypes.ANNOTATION;
    }

}
