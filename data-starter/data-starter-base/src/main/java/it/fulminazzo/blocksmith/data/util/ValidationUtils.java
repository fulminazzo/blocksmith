package it.fulminazzo.blocksmith.data.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A collection of utilities to validate data.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationUtils {
    static final int MIN_PORT = 1;
    static final int MAX_PORT = 65535;

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

}
