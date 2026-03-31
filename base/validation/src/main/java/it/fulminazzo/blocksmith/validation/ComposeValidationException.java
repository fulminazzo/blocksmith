package it.fulminazzo.blocksmith.validation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * Special kind of validation exception that aggregates multiple violations for one object fields.
 */
public final class ComposeValidationException extends Exception {
    @Getter
    private final @Nullable Object object;
    @Getter
    private final @NotNull Map<Field, Set<ConstraintViolation>> violations;

    /**
     * Instantiates a new Compose validation exception.
     *
     * @param object     the object that caused the exception
     * @param violations the violations
     */
    public ComposeValidationException(final @Nullable Object object, final @NotNull Map<Field, Set<ConstraintViolation>> violations) {
        super(String.format("Validation failed for object %s: %s constraint(s) violated", object, violations.size()));
        this.object = object;
        this.violations = violations;
    }

}
