package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An exception to aggregate multiple {@link ConstraintViolation}s.
 */
public final class ViolationException extends RuntimeException {
    private static final long serialVersionUID = 1428474027468269097L;

    /**
     * Instantiates a new Violation exception.
     *
     * @param cause the cause exception
     */
    ViolationException(final @NotNull ValidationException cause) {
        this(cause.getViolations());
    }

    /**
     * Instantiates a new Violation exception.
     *
     * @param violations the violations
     */
    ViolationException(final @NotNull Map<String, Set<ConstraintViolation>> violations) {
        super(violations.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("invalid %s: %s",
                        entry.getKey(), entry.getValue().stream()
                                .map(ConstraintViolation::getExceptionMessage)
                                .collect(Collectors.joining(", "))))
                .collect(Collectors.joining("; "))
        );
    }

}
