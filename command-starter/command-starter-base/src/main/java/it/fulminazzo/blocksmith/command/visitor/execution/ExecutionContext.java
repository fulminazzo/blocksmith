package it.fulminazzo.blocksmith.command.visitor.execution;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.visitor.VisitorContext;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * Represents the context of execution of a command.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExecutionContext extends VisitorContext {
    @NotNull LinkedList<Object> arguments = new LinkedList<>();

    /**
     * Instantiates a new Execution context.
     *
     * @param commandSender the command sender
     */
    public ExecutionContext(final @NotNull CommandSenderWrapper<?> commandSender) {
        super(commandSender);
    }

    /**
     * Adds a new argument to the internal pool.
     *
     * @param argument the argument
     * @return this object (for method chaining)
     */
    public @NotNull ExecutionContext addArgument(final @Nullable Object argument) {
        arguments.add(argument);
        return this;
    }

}
