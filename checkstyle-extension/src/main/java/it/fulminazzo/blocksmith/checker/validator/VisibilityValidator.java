package it.fulminazzo.blocksmith.checker.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * {@link RankerValidator} implementation for validating visibility modifiers.
 *
 * @see RankerValidator
 */
public enum VisibilityValidator implements RankerValidator {
    /**
     * {@code public} validator.
     */
    PUBLIC {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return NodeUtils.isModifierPresent(node, TokenTypes.LITERAL_PUBLIC);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "order.public";
        }
    },
    /**
     * {@code protected} validator.
     */
    PROTECTED {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return NodeUtils.isModifierPresent(node, TokenTypes.LITERAL_PROTECTED);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "order.protected";
        }
    },
    /**
     * Package (not {@code public}, {@code protected} or {@code private}) validator.
     */
    PACKAGE {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return !NodeUtils.isModifierPresent(node, TokenTypes.LITERAL_PUBLIC)
                    && !NodeUtils.isModifierPresent(node, TokenTypes.LITERAL_PROTECTED)
                    && !NodeUtils.isModifierPresent(node, TokenTypes.LITERAL_PRIVATE);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "order.package";
        }
    },
    /**
     * {@code private} validator.
     */
    PRIVATE {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return NodeUtils.isModifierPresent(node, TokenTypes.LITERAL_PRIVATE);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "order.private";
        }
    }

}
