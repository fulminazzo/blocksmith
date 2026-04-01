package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A Constraint validator for {@link CharSequence} types.
 */
final class StringConstraintValidator extends ConstraintValidatorImpl {

    /**
     * Instantiates a new String constraint validator.
     *
     * @param validPredicate the valid predicate
     */
    public StringConstraintValidator(final @NotNull Predicate<Object> validPredicate) {
        super(validPredicate, CharSequence.class);
    }

    @Override
    public @NotNull String getTypeNames() {
        return "string (or any character sequence)";
    }

}
