package it.fulminazzo.blocksmith.naming;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a {@link NamingConvention} that uses a separator to tokenize and format the input.
 */
@RequiredArgsConstructor
final class SeparatedCaseConvention implements NamingConvention {
    private final @NotNull String separator;

    @Override
    public @NotNull List<String> tokenize(final @NotNull String input) {
        return Arrays.asList(input.split(separator));
    }

    @Override
    public @NotNull String format(final @NotNull List<String> tokens) {
        return String.join(separator, tokens);
    }

}
