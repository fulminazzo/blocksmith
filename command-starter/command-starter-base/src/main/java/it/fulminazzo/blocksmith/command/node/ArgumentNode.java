package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    protected void validateInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        context.addParsedArgument(parseArgument(context.getCurrent()));
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return true;
    }

}
