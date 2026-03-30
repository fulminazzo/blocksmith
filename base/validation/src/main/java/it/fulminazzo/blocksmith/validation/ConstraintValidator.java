package it.fulminazzo.blocksmith.validation;

/**
 * A function to validate objects.
 */
@FunctionalInterface
public interface ConstraintValidator {

    /**
     * Validates the given object.
     *
     * @param value the value
     * @return <code>true</code> if the value is valid, <code>false</code> otherwise
     */
    boolean isValid(final Object value);

}
