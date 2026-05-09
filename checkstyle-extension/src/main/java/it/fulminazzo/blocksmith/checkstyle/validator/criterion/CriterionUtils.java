package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * A collection of utilities for {@link DetailAST}.
 */
final class CriterionUtils {

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
