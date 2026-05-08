package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.jetbrains.annotations.NotNull;

/**
 * Marks an object capable of validating a node for {@link RankerImpl}.
 *
 * @see RankerImpl
 */
public interface RankerValidator {

    /**
     * Validates the given node.
     *
     * @param node the node to validate
     * @return {@code true} if the node is valid, {@code false} otherwise
     */
    boolean validate(final @NotNull DetailAST node);

}
