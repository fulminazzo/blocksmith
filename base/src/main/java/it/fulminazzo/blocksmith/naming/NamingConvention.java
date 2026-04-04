package it.fulminazzo.blocksmith.naming;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a naming convention.
 */
public interface NamingConvention {

    /**
     * Converts the given string to the target naming convention.
     *
     * @param input the input
     * @param target the naming convention to convert to
     * @return the converted string
     */
    default @NotNull String convertTo(final @NotNull String input, final @NotNull NamingConvention target) {
        return target.format(tokenize(input));
    }

    /**
     * Splits the string into tokens according to the convention rules.
     *
     * @param input the input
     * @return the tokens
     */
    @NotNull List<String> tokenize(final @NotNull String input);

    /**
     * Converts the tokens to a string in the current convention.
     *
     * @param tokens the tokens
     * @return the string
     */
    @NotNull String format(final @NotNull List<String> tokens);

}
