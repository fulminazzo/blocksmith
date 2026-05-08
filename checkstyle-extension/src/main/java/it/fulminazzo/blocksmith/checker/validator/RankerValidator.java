package it.fulminazzo.blocksmith.checker.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checker.NodeValidator;
import org.jetbrains.annotations.NotNull;

/**
 * Marks an object capable of validating nodes for {@link NodeValidator}.
 *
 * @see NodeValidator
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
