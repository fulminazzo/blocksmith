package it.fulminazzo.blocksmith.checker.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * {@link RankerValidator} implementation for validating the {@code final} keyword.
 *
 * @see RankerValidator
 */
public enum MutabilityValidator implements RankerValidator {
    /**
     * {@code final} validator.
     */
    FINAL {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return node.findFirstToken(TokenTypes.FINAL) != null;
        }
    },
    /**
     * Non-{@code final} validator.
     */
    NON_FINAL {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return node.findFirstToken(TokenTypes.FINAL) == null;
        }
    }

}
