package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * An exception to aggregate multiple {@link ConstraintViolation}s.
 */
public final class ViolationException extends RuntimeException {

    /**
     * Instantiates a new Violation exception.
     *
     * @param violations the violations that triggered the exception
     */
    ViolationException(final @NotNull Set<ConstraintViolation> violations) {
        super(violations.stream()
                .map(ConstraintViolation::getExceptionMessage)
                .collect(Collectors.joining(", "))
        );
    }

}
