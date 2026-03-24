package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import lombok.*;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public class ArgumentNode<T> extends CommandNode {
    private final @NotNull String name;
    private final @NotNull Class<T> type;
    private final boolean optional;
    @Getter(AccessLevel.NONE)
    private @Nullable String defaultValue;
    private boolean greedy;

    /**
     * Gets the default value (if given).
     *
     * @param context the current context of action
     * @return the default value
     * @throws CommandExecutionException in case of parsing errors
     */
    public @Nullable T getDefaultValue(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        return defaultValue == null ? null : parseArgument(context.addInput(defaultValue));
    }

    private @Nullable T parseArgument(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        return ArgumentParsers.of(type).parse(context);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
        return ArgumentParsers.of(type).getCompletions(context).stream()
                .map(c -> c.replace("%name%", getName()))
                .collect(Collectors.toList());
    }

    @Override
    protected void validateExecuteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        if (isGreedy()) context.mergeRemainingInput();
        context.addParsedArgument(parseArgument(context));
    }

    @Override
    protected void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        if (isGreedy()) context.mergeRemainingInput();
        if (!ArgumentParsers.of(type).validateCompletions(context))
            throw new CommandExecutionException();
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return true;
    }

    /**
     * Instantiates a new Argument node.
     *
     * @param <T>      the type of the parameter
     * @param name     the name
     * @param type     the Java class of the parameter
     * @param optional if <code>true</code> the parameter will be non-mandatory
     * @return the argument node
     */
    public static <T> @NotNull ArgumentNode<T> newNode(final @NotNull String name,
                                                       final @NotNull Class<T> type,
                                                       final boolean optional) {
        return new ArgumentNode<>(name, type, optional);
    }

}
