package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

/**
 * A function to validate objects.
 */
public interface ConstraintValidator {

    /**
     * Checks that the given object is of the expected types.
     *
     * @param value the value
     * @return <code>true</code> if it is, <code>false</code> otherwise
     */
    boolean matches(final Object value);

    /**
     * Returns the name of the types expected from this validator.
     *
     * @return the names
     */
    @NotNull String getTypeNames();

    /**
     * Validates the given object.
     *
     * @param value the value
     * @return <code>true</code> if the value is valid, <code>false</code> otherwise
     */
    boolean isValid(final Object value);

}
