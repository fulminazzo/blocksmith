package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A function to validate objects.
 */
@FunctionalInterface
public interface Validator {

    /**
     * Validates the given object.
     *
     * @param value               the value
     * @param errorMessage        the error message to include in the ConstraintViolation
     * @param defaultErrorMessage the default error message to fall back to in the ConstraintViolation
     * @param data                additional data used by the validator to validate the value
     * @return the constraint violation if the value is invalid, empty otherwise
     */
    @NotNull Optional<ConstraintViolation> validate(final Object value,
                                                    final @Nullable String errorMessage,
                                                    final @NotNull String defaultErrorMessage,
                                                    final @Nullable Object @NotNull ... data);

}
