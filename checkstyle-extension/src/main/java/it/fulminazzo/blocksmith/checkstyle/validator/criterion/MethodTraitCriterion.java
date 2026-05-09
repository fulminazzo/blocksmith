package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Criterion} implementation for the characteristics of a method.
 *
 * @see Criterion
 */
public enum MethodTraitCriterion implements Criterion {
    /**
     * {@code abstract} criterion.
     */
    ABSTRACT {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.isModifierPresent(node, TokenTypes.ABSTRACT);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.abstract";
        }
    },
    /**
     * Concrete (not {@link #ABSTRACT}, {@link #OVERRIDE} or {@link #STATIC}) criterion.
     */
    CONCRETE {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return !CriterionUtils.isModifierPresent(node, TokenTypes.ABSTRACT)
                    && !CriterionUtils.isAnnotatedWith(node, "Override")
                    && !CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_STATIC);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.concrete";
        }
    },
    /**
     * {@code @Override} annotation criterion.
     */
    OVERRIDE {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.isAnnotatedWith(node, "Override");
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.override";
        }
    },
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
            return "method.static";
        }
    }

}
