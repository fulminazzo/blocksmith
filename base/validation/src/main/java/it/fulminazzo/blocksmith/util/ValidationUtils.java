package it.fulminazzo.blocksmith.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * A collection of utilities to validate data.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationUtils {
    /**
     * The minimum allowed port.
     */
    static final int MIN_PORT = 1;
    /**
     * The maximum allowed port.
     */
    static final int MAX_PORT = 65535;

    private static final @NotNull Validator validator;

    static {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ValidationUtils.class.getClassLoader());
            try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
                validator = factory.getValidator();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    /**
     * Uses <a href="https://beanvalidation.org/">Jakarta Validation</a> to verify
     * the correctness of the given object.
     *
     * @param <T>    the type of the object
     * @param object the object
     * @throws ViolationException in case of failed validation
     */
    public static <T> void validate(final @NotNull T object) throws ViolationException {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        Optional<ConstraintViolation<T>> first = violations.stream().findFirst();
        if (first.isPresent()) throw new ViolationException(first.get());
    }

    /**
     * Uses <a href="https://beanvalidation.org/">Jakarta Validation</a> to verify
     * the correctness of the given field.
     *
     * @param <T>        the type of the class
     * @param type       the Java class that contains the field
     * @param fieldName  the name of the field
     * @param fieldValue the value of the field (to validate)
     * @throws ViolationException in case of failed validation
     */
    public static <T> void validateField(final @NotNull Class<T> type,
                                         final @NotNull String fieldName,
                                         final @NotNull Object fieldValue) throws ViolationException {
        Set<ConstraintViolation<T>> violations = validator.validateValue(type, fieldName, fieldValue);
        Optional<ConstraintViolation<T>> first = violations.stream().findFirst();
        if (first.isPresent()) throw new ViolationException(first.get());
    }

    /**
     * Checks if the given value is positive and not <code>0</code>.
     *
     * @param value     the value
     * @param valueName the value name
     */
    public static void checkNatural(final long value,
                                    final @NotNull String valueName) {
        checkGreaterEqualThan(value, valueName, 1);
    }

    /**
     * Checks if the given value is positive.
     *
     * @param value     the value
     * @param valueName the value name
     */
    public static void checkPositive(final long value,
                                     final @NotNull String valueName) {
        checkGreaterEqualThan(value, valueName, 0);
    }

    /**
     * Checks if the given value is greater or equal than the minimum value.
     *
     * @param value     the value
     * @param valueName the value name
     * @param min       the minimum allowed value
     */
    public static void checkGreaterEqualThan(final long value,
                                             final @NotNull String valueName,
                                             final long min) {
        if (value < min)
            throw new IllegalArgumentException(String.format("Invalid %s %s. Must be at least %s",
                    valueName, value, min
            ));
    }

    /**
     * Checks if the given value is a valid port.
     *
     * @param port the port
     */
    public static void checkPort(final int port) {
        checkInRange(port, "port", MIN_PORT, MAX_PORT);
    }

    /**
     * Checks if the given value is in range.
     *
     * @param value     the value
     * @param valueName the value name
     * @param min       the minimum allowed value
     * @param max       the maximum allowed value
     */
    public static void checkInRange(final long value,
                                    final @NotNull String valueName,
                                    final long min,
                                    final long max) {
        if (value < min || value > max)
            throw new IllegalArgumentException(String.format("Invalid %s %s. Must be between %s and %s",
                    valueName, value, min, max
            ));
    }

    /**
     * Represents an exception thrown during a failed validation.
     */
    public static final class ViolationException extends RuntimeException {

        /**
         * Instantiates a new Violation exception.
         *
         * @param constraintViolation the constraint violation that triggered the exception
         */
        public ViolationException(final @NotNull ConstraintViolation<?> constraintViolation) {
            super(constraintViolation.getMessage());
        }

    }

}
