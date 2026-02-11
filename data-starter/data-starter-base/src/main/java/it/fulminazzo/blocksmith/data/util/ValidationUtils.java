package it.fulminazzo.blocksmith.data.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A collection of utilities to validate data.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationUtils {

    /**
     * Checks if the given value is in range.
     *
     * @param value     the value
     * @param valueName the value name
     * @param min       the minimum allowed value
     * @param max       the maximum allowed value
     */
    public static void checkInRange(final int value,
                                    final @NotNull String valueName,
                                    final int min,
                                    final int max) {
        if (value < min || value > max)
            throw new IllegalArgumentException(String.format("Invalid %s %s. Must be between %s and %s",
                    valueName, value, min, max
            ));
    }

}
