package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * A Constraint validator for {@link Number} or {@link Duration} types.
 */
final class NumberDurationConstraintValidator extends ConstraintValidatorImpl {

    /**
     * Instantiates a new Number constraint validator.
     *
     * @param validPredicate the valid predicate
     */
    public NumberDurationConstraintValidator(final @NotNull Predicate<@NotNull Double> validPredicate) {
        super(o -> {
            if (o == null) return true;
            else if (o instanceof Duration) o = ((Duration) o).toMillis();
            return validPredicate.test(((Number) o).doubleValue());
        }, Number.class, Duration.class);
    }

    @Override
    public @NotNull String getTypeNames() {
        return "number or time duration";
    }

}
