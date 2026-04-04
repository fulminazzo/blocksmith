package it.fulminazzo.blocksmith.naming;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a naming convention.
 */
interface NamingConvention {

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
