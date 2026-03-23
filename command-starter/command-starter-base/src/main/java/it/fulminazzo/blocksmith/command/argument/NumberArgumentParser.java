package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
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
final class NumberArgumentParser<N extends Number> implements ArgumentParser<N> {
    private final @NotNull N min;
    private final @NotNull N max;
    private final @NotNull Function<String, N> parser;

    @Override
    public @Nullable N parse(final @NotNull String rawArgument) throws CommandExecutionException {
        try {
            return parser.apply(rawArgument);
        } catch (NumberFormatException e) {
            throw new CommandExecutionException("error.invalid-number")
                    .arguments(Placeholder.of("min", min), Placeholder.of("max", max));
        }
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
        String argument = context.getCurrent();
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
            parser.apply(argument);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

}
