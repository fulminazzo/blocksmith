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
     * Converts the input to the target one.
     * Assumes that the input is formatted in the {@link Convention#CAMEL_CASE} convention.
     *
     * @param input      the input
     * @param convention the convention to convert to
     * @return the converted string
     */
    public static @NotNull String convert(final @NotNull String input,
                                          final @NotNull Convention convention) {
        return convert(input, Convention.CAMEL_CASE, convention);
    }

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
        NamingConvention fromConvention = from.getConvention();
        NamingConvention toConvention = to.getConvention();
        return toConvention.format(fromConvention.tokenize(input));
    }

}
