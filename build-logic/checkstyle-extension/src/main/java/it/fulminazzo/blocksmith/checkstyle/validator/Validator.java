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

    /**
     * Enters a new computation scope to allow isolated verifications.
     */
    void enterScope();

    /**
     * Exits the current computation scope.
     * <br>
     * If any {@link DetailAST} has been recorded in the last scope through {@link #validateNode(DetailAST)},
     * each group formed with the <b>same scores</b> will be passed through the next {@link Validator}.
     *
     * @throws CompositeValidationException if any of those nodes is not valid
     */
    void exitScope() throws CompositeValidationException;

}
