package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checkstyle.validator.PairedMethods;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Criterion} implementation for grouping based on the name of a method.
 */
public enum MethodNameCriterion implements Criterion {
    /**
     * General name criterion.
     */
    ANY {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return !GETTER_AND_SETTER.matches(node)
                    && !EQUALS.matches(node)
                    && !HASH_CODE.matches(node)
                    && !TO_STRING.matches(node);
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.any";
        }
    },
    /**
     * {@code (get|is)<name>} and {@code set<name>} criterion.
     */
    GETTER_AND_SETTER {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            String methodName = CriterionUtils.getMethodName(node);
            return PairedMethods.methodNameStartsWith(
                    methodName,
                    PairedMethods.GETTERS,
                    PairedMethods.SETTERS
            );
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.getterAndSetter";
        }
    },
    /**
     * {@code equals} criterion.
     */
    EQUALS {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.getMethodName(node).equals("equals");
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.equals";
        }
    },
    /**
     * {@code hashCode} criterion.
     */
    HASH_CODE {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.getMethodName(node).equals("hashCode");
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.hashCode";
        }
    },
    /**
     * {@code toString} criterion.
     */
    TO_STRING {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return CriterionUtils.getMethodName(node).equals("toString");
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.toString";
        }
    }

}
