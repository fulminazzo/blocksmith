package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Criterion} implementation for the {@code final} keyword.
 *
 * @see Criterion
 */
public enum MutabilityCriterion implements Criterion {
    /**
     * {@code final} criterion.
     */
    FINAL {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.isModifierPresent(node, TokenTypes.FINAL);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "final";
        }
    },
    /**
     * Non-{@code final} criterion.
     */
    NON_FINAL {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return !CriterionUtils.isModifierPresent(node, TokenTypes.FINAL);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "non-final";
        }
    }

}
