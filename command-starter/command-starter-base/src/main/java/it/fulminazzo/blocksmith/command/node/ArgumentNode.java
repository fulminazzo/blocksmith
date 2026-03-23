package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a dynamic argument node.
 *
 * @param <T> the type of the argument
 */
@Data
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class ArgumentNode<T> extends CommandNode {
    private final @NotNull String name;
    private final @NotNull Class<T> type;
    private final boolean optional;
    private @Nullable String defaultValue;
    private boolean greedy;

    /**
     * Gets the default value (if given).
     *
     * @return the default value
     * @throws CommandExecutionException in case of parsing errors
     */
    public @Nullable T getDefaultValue() throws CommandExecutionException {
        return defaultValue == null ? null : parseArgument(defaultValue);
    }

    private @Nullable T parseArgument(final @NotNull String argument) throws CommandExecutionException {
        return ArgumentParsers.of(type).parse(argument);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
        return ArgumentParsers.of(type).getCompletions(context).stream()
                .map(c -> c.replace("%name%", getName()))
                .collect(Collectors.toList());
    }

    @Override
    protected void validateInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        StringBuilder current = new StringBuilder(context.getCurrent());
        if (isGreedy()) {
            context.advanceCursor();
            while (!context.isDone()) {
                current.append(" ").append(context.getCurrent());
                context.advanceCursor();
            }
        }
        context.addParsedArgument(parseArgument(current.toString()));
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return true;
    }

}
