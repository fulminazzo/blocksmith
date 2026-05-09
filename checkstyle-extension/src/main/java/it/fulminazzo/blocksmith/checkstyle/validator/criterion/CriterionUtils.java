package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * A collection of utilities for {@link DetailAST}.
 */
final class CriterionUtils {

    /**
     * Checks if the element is annotated with the given annotation.
     *
     * @param node       the node to check
     * @param annotation the annotation
     * @return {@code true} if it is annotated, {@code false} otherwise
     */
    public static boolean isAnnotatedWith(final @NotNull DetailAST node, final @NotNull String annotation) {
        DetailAST modifier = node.findFirstToken(TokenTypes.MODIFIERS);
        if (modifier == null) return false;

        for (DetailAST annotationNode = modifier.findFirstToken(TokenTypes.ANNOTATION);
                annotationNode != null;
                annotationNode = annotationNode.getNextSibling()) {
            DetailAST ident = annotationNode.findFirstToken(TokenTypes.IDENT);
            if (ident != null && annotation.equals(ident.getText())) return true;
        }
        return false;
    }

    /**
     * Checks if a modifier has been declared in the given node.
     *
     * @param node     the node to check
     * @param modifier the modifier
     * @return {@code true} if the modifier is present, {@code false} otherwise
     */
    public static boolean isModifierPresent(final @NotNull DetailAST node, final int modifier) {
        DetailAST modifiers = node.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) return false;

        for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == modifier) return true;
        }
        return false;
    }

}
