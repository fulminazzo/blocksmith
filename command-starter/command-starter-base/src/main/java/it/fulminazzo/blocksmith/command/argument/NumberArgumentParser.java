package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A {@link ArgumentParser} for general {@link Number} objects.
 *
 * @param <N> the type of the parameter
 */
@RequiredArgsConstructor
public final class NumberArgumentParser<N extends Number> implements ArgumentParser<N> {
    private final @NotNull N min;
    private final @NotNull N max;
    @Getter
    private final @NotNull Function<String, N> parser;

    @Override
    public @Nullable N parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
        String rawArgument = visitor.getInput().getCurrent();
        try {
            return parser.apply(rawArgument);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(CommandMessages.INVALID_NUMBER)
                    .arguments(
                            Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, rawArgument),
                            Placeholder.of("min", min),
                            Placeholder.of("max", max)
                    );
        }
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        String argument = visitor.getInput().getCurrent();
        List<String> completions = new ArrayList<>();
        if (argument.isEmpty() || isValid(argument))
            for (int i = 0; i < 10; i++) {
                String tmp = argument + i;
                if (isValid(tmp)) completions.add(tmp);
            }
        return completions;
    }

    private boolean isValid(final @NotNull String argument) {
        try {
            N parsed = parser.apply(argument);
            return parsed.floatValue() != Float.POSITIVE_INFINITY && parsed.floatValue() != Float.NEGATIVE_INFINITY;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

}
