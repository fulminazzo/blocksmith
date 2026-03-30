package it.fulminazzo.blocksmith.validation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * An exception thrown when validation fails.
 */
public final class ValidationException extends Exception {
    @Getter
    private final @NotNull Set<ConstraintViolation> violations;

    /**
     * Instantiates a new Validation exception.
     *
     * @param object     the object that caused the exception
     * @param violations the violations
     */
    ValidationException(final Object object, final @NotNull Set<ConstraintViolation> violations) {
        super(String.format("Validation failed for object %s: %s constraint(s) violated", object, violations.size()));
        this.violations = violations;
    }

    /**
     * Instantiates a new Validation exception.
     *
     * @param object the object
     * @param cause  the cause that triggered the exception
     */
    ValidationException(final Object object, final @NotNull Throwable cause) {
        super(String.format("Could not complete validation for object %s", object), cause);
        this.violations = Set.of();
    }

}
