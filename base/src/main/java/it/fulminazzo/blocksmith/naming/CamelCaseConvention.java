package it.fulminazzo.blocksmith.naming;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class CamelCaseConvention implements NamingConvention {

    @Override
    public @NotNull List<String> tokenize(final @NotNull String input) {
        return Arrays.stream(input.split("(?=[A-Z])"))
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull String format(final @NotNull List<String> tokens) {
        StringBuilder builder = new StringBuilder();
        if (!tokens.isEmpty()) builder.append(tokens.get(0));
        for (int i = 1; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.isEmpty()) builder.append(token);
            else builder.append(token.substring(0, 1).toUpperCase())
                    .append(token.substring(1));
        }
        return builder.toString();
    }

}
