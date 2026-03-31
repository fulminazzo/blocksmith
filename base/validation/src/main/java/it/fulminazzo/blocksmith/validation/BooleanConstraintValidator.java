package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A Constraint validator for {@link Boolean} types.
 */
final class BooleanConstraintValidator extends ConstraintValidatorImpl {

    /**
     * Instantiates a new Boolean constraint validator.
     *
     * @param validPredicate the valid predicate
     */
    public BooleanConstraintValidator(final @NotNull Predicate<Object> validPredicate) {
        super(validPredicate, Boolean.class);
    }

    @Override
    public @NotNull String getTypeNames() {
        return "true or false";
    }

}
