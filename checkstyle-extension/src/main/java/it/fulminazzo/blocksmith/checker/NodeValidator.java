package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.jetbrains.annotations.NotNull;

/**
 * An object to validate nodes against multiple rulesets.
 *
 * @see NodeOrderValidator
 */
public interface NodeValidator extends Ranker {

    /**
     * Validates the given node with these validator rulesets.
     *
     * @param node the node to validate
     * @throws ValidationException if the node is not valid
     */
    void validateNode(final @NotNull DetailAST node) throws ValidationException;

    /**
     * Adds a validator to the chain of validators.
     *
     * @param validator the validator to add
     * @return this object (for method chaining)
     */
    @NotNull NodeValidator then(final @NotNull NodeValidator validator);

}
