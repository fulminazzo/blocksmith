package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Criterion} implementation for the {@code static} keyword.
 *
 * @see Criterion
 */
public enum StaticCriterion implements Criterion {
    /**
     * {@code static} criterion.
     */
    STATIC {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_STATIC);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "static";
        }
    },
    /**
     * Non-{@code static} criterion.
     */
    NON_STATIC {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return !CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_STATIC);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "non-static";
        }
    }

}
