package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

/**
 * An exception to aggregate multiple {@link ConstraintViolation}s.
 */
public final class ViolationException extends RuntimeException {

    /**
     * Instantiates a new Violation exception.
     *
     * @param object the invalid object
     * @param cause  the cause exception
     */
    ViolationException(final @Nullable Object object, final @NotNull ComposeValidationException cause) {
        super(String.format("Invalid object '%s': %s",
                object, cause.getViolations().entrySet().stream()
                        .map(entry -> String.format("invalid field '%s': %s",
                                entry.getKey().getName(), entry.getValue().stream()
                                        .map(ConstraintViolation::getExceptionMessage)
                                        .collect(Collectors.joining(", "))))
                        .collect(Collectors.joining("; "))
        ));
    }

    /**
     * Instantiates a new Violation exception.
     *
     * @param object the invalid object
     * @param cause  the cause exception
     */
    ViolationException(final @Nullable Object object, final @NotNull ValidationException cause) {
        super(String.format("Invalid object '%s': %s",
                object, cause.getViolations().stream()
                        .map(ConstraintViolation::getExceptionMessage)
                        .collect(Collectors.joining(", "))
        ));
    }

}
