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
            for (MethodNameCriterion value : MethodNameCriterion.values()) {
                if (value != this && value.matches(node)) return false;
            }
            return true;
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.any";
        }
    },
    /**
     * All adders and all removers criterion.
     */
    ALL_ADDERS_AND_ALL_REMOVERS {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            String methodName = CriterionUtils.getMethodName(node);
            return PairedMethods.isMethodPairedWith(methodName, PairedMethods.getAllAddersAndRemovers());
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.pairedMethods";
        }
    },
    /**
     * Registerers and unregisterers criterion.
     */
    REGISTERER_AND_UNREGISTERER {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            String methodName = CriterionUtils.getMethodName(node);
            return PairedMethods.isMethodPairedWith(methodName, PairedMethods.getRegisterersAndUnregisterers());
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.pairedMethods";
        }
    },
    /**
     * Adders and removers criterion.
     */
    ADDER_AND_REMOVER {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            String methodName = CriterionUtils.getMethodName(node);
            return PairedMethods.isMethodPairedWith(methodName, PairedMethods.getAddersAndRemovers());
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.pairedMethods";
        }
    },
    /**
     * Getters and setters criterion.
     */
    GETTER_AND_SETTER {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            String methodName = CriterionUtils.getMethodName(node);
            return PairedMethods.isMethodPairedWith(methodName, PairedMethods.getGettersAndSetters());
        }

        @Override
        public @NotNull String getErrorMessage() {
            return "method.name.pairedMethods";
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
