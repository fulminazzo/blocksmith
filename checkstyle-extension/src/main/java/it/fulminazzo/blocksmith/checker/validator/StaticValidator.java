package it.fulminazzo.blocksmith.checker.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * {@link RankerValidator} implementation for validating the {@code static} keyword.
 *
 * @see RankerValidator
 */
public enum StaticValidator implements RankerValidator {
    /**
     * {@code static} validator.
     */
    STATIC {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return NodeUtils.isModifierPresent(node, TokenTypes.LITERAL_STATIC);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "order.static";
        }
    },
    /**
     * Non-{@code static} validator.
     */
    NON_STATIC {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return !NodeUtils.isModifierPresent(node, TokenTypes.LITERAL_STATIC);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "order.non-static";
        }
    }

}
