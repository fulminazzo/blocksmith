package it.fulminazzo.blocksmith.naming;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * The main entry point of this package classes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CaseConverter {

    /**
     * Converts the input from the given naming convention to the target one.
     *
     * @param input the input
     * @param from  the convention to convert from
     * @param to    the convention to convert to
     * @return the converted string
     */
    public static @NotNull String convert(final @NotNull String input,
                                          final @NotNull Convention from,
                                          final @NotNull Convention to) {
        return from.convertTo(input, to);
    }

}
