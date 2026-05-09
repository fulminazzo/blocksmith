package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.jetbrains.annotations.NotNull;

/**
 * An object to validate nodes against multiple rulesets.
 *
 * @see OrderValidator
 */
public interface Validator {

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
    @NotNull Validator then(final @NotNull Validator validator);

}
