package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * A Constraint validator for {@link Number} types.
 */
final class NumberConstraintValidator extends ConstraintValidatorImpl {

    /**
     * Instantiates a new Number constraint validator.
     *
     * @param validPredicate the valid predicate
     */
    public NumberConstraintValidator(final @NotNull Predicate<Object> validPredicate) {
        super(validPredicate, Number.class, Duration.class);
    }

    @Override
    public @NotNull String getTypeNames() {
        return "number or time duration";
    }

}
