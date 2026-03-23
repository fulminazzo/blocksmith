package it.fulminazzo.blocksmith.command.execution;

import it.fulminazzo.blocksmith.message.argument.Argument;
import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An exception thrown during execution of command nodes.
 */
@StandardException
public final class CommandExecutionException extends Exception {
    private final @NotNull List<Argument> arguments = new ArrayList<>();

    /**
     * Adds new arguments to the final message.
     *
     * @param arguments the arguments
     * @return this object (for method chaining)
     */
    public @NotNull CommandExecutionException arguments(final Argument @NotNull ... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
        return this;
    }

    /**
     * Get the arguments.
     *
     * @return the arguments
     */
    public @NotNull Argument[] getArguments() {
        return arguments.toArray(new Argument[0]);
    }

}
