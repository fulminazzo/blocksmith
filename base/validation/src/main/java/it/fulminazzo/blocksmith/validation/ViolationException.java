package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * An exception to aggregate multiple {@link ConstraintViolation}s.
 */
public final class ViolationException extends RuntimeException {

    /**
     * Instantiates a new Violation exception.
     *
     * @param cause the cause exception
     */
    ViolationException(final @NotNull ComposeValidationException cause) {
        super(cause.getViolations().entrySet().stream()
                .map(entry -> String.format("invalid property '%s': %s",
                        entry.getKey(), entry.getValue().stream()
                                .map(ConstraintViolation::getExceptionMessage)
                                .collect(Collectors.joining(", "))))
                .collect(Collectors.joining("; "))
        );
    }

    /**
     * Instantiates a new Violation exception.
     *
     * @param cause the cause exception
     */
    ViolationException(final @NotNull ValidationException cause) {
        super(cause.getViolations().stream()
                .map(ConstraintViolation::getExceptionMessage)
                .collect(Collectors.joining(", "))
        );
    }

}
