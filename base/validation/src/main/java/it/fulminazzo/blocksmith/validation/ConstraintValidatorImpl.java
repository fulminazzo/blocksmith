package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Implementation of {@link ConstraintValidator}.
 */
@FunctionalInterface
interface ConstraintValidatorImpl extends ConstraintValidator {

    @Override
    default @NotNull Optional<ConstraintViolation> validate(final Object value,
                                                            final @Nullable String errorMessage,
                                                            final @NotNull String defaultErrorMessage,
                                                            final @Nullable Object @NotNull ... data) {
        if (validate(value, data)) return Optional.empty();
        else return Optional.of(new ConstraintViolation(value, errorMessage, defaultErrorMessage));
    }

    /**
     * Validates the given object.
     *
     * @param value the value
     * @param data  additional data used by the validator to validate the value
     * @return <code>true</code> if it is valid
     */
    boolean validate(final Object value, final @Nullable Object @NotNull ... data);

}
