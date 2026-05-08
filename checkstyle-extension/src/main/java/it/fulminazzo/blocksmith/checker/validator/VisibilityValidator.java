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
            return node.findFirstToken(TokenTypes.LITERAL_PUBLIC) != null;
        }
    },
    /**
     * {@code protected} validator.
     */
    PROTECTED {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return node.findFirstToken(TokenTypes.LITERAL_PROTECTED) != null;
        }
    },
    /**
     * Package (not {@code public}, {@code protected} or {@code private}) validator.
     */
    PACKAGE {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return node.findFirstToken(TokenTypes.LITERAL_PUBLIC) == null
                    && node.findFirstToken(TokenTypes.LITERAL_PROTECTED) == null
                    && node.findFirstToken(TokenTypes.LITERAL_PRIVATE) == null;
        }
    },
    /**
     * {@code private} validator.
     */
    PRIVATE {
        @Override
        public boolean validate(final @NotNull DetailAST node) {
            return node.findFirstToken(TokenTypes.LITERAL_PRIVATE) != null;
        }
    }

}
