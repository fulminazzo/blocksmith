package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An exception to aggregate multiple {@link ConstraintViolation}s.
 */
public final class ViolationException extends RuntimeException {

    /**
     * Instantiates a new Violation exception.
     *
     * @param messageFormat the format to use for the message
     * @param cause         the cause exception
     */
    ViolationException(final @NotNull String messageFormat,
                       final @NotNull ValidationException cause) {
        this(messageFormat, cause.getViolations());
    }

    /**
     * Instantiates a new Violation exception.
     *
     * @param messageFormat the format to use for the message
     * @param violations    the violations
     */
    ViolationException(final @NotNull String messageFormat,
                       final @NotNull Map<String, Set<ConstraintViolation>> violations) {
        super(violations.entrySet().stream()
                .map(entry -> String.format(messageFormat,
                        entry.getKey(), entry.getValue().stream()
                                .map(ConstraintViolation::getExceptionMessage)
                                .collect(Collectors.joining(", "))))
                .collect(Collectors.joining("; "))
        );
    }

}
