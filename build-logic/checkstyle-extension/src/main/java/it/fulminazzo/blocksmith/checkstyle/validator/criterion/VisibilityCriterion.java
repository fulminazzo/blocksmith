package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Criterion} implementation for visibility modifiers.
 *
 * @see Criterion
 */
public enum VisibilityCriterion implements Criterion {
    /**
     * {@code public} criterion.
     */
    PUBLIC {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_PUBLIC);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "public";
        }
    },
    /**
     * {@code protected} criterion.
     */
    PROTECTED {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_PROTECTED);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "protected";
        }
    },
    /**
     * Package (not {@link #PUBLIC}, {@link #PROTECTED} or {@link #PRIVATE}) criterion.
     */
    PACKAGE {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return !PUBLIC.matches(node) && !PROTECTED.matches(node) && !PRIVATE.matches(node);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "package";
        }
    },
    /**
     * {@code private} criterion.
     */
    PRIVATE {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_PRIVATE);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "private";
        }
    }

}
