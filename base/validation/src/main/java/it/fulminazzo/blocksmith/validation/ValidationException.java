package it.fulminazzo.blocksmith.validation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * An exception thrown when validation fails.
 */
public final class ValidationException extends Exception {
    @Getter
    private final @Nullable Object object;
    @Getter
    private final @NotNull Map<String, Set<ConstraintViolation>> violations;

    /**
     * Instantiates a new Compose validation exception.
     *
     * @param object     the object that caused the exception
     * @param violations the violations (a map containing the fields path and the violations for that field)
     *
     */
    public ValidationException(final @Nullable Object object, final @NotNull Map<String, Set<ConstraintViolation>> violations) {
        super(String.format("Validation failed for object %s: %s constraint(s) violated", object, violations.size()));
        this.object = object;
        this.violations = violations;
    }

}
