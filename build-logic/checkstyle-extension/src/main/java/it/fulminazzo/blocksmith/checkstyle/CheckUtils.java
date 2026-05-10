package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A collection of utilities for checks.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CheckUtils {

    /**
     * Checks if the node is a field.
     *
     * @param node the node to check
     * @return {@code true} if it is, {@code false} otherwise
     */
    public static boolean isField(final @NotNull DetailAST node) {
        return node.getParent().getType() == TokenTypes.OBJBLOCK && node.getType() == TokenTypes.VARIABLE_DEF;
    }

}
