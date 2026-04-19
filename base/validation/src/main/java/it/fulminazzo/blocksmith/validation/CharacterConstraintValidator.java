package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A Constraint validator for {@link Character} types.
 */
final class CharacterConstraintValidator extends ConstraintValidatorImpl {

    /**
     * Instantiates a new Character constraint validator.
     *
     * @param validPredicate the valid predicate
     */
    public CharacterConstraintValidator(final @NotNull Predicate<@NotNull Character> validPredicate) {
        super(o -> o == null || validPredicate.test((Character) o), Character.class);
    }

    @Override
    public @NotNull String getTypeNames() {
        return "character";
    }

}
