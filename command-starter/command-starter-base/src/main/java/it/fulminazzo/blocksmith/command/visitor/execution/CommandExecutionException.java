package it.fulminazzo.blocksmith.command.visitor.execution;

import it.fulminazzo.blocksmith.message.argument.Argument;
import lombok.Getter;
import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An exception thrown during execution of command nodes.
 */
@StandardException
public final class CommandExecutionException extends Exception {
    private static final long serialVersionUID = 5460537445633897473L;

    private final @NotNull List<Argument> arguments = new ArrayList<>();
    @Getter
    private final @NotNull Map<String, Argument[]> additionalMessages = new HashMap<>();

    /**
     * Adds another message to be sent when this exception is thrown.
     *
     * @param message   the message
     * @param arguments the arguments (applied to the message)
     * @return this object (for method chaining)
     */
    public @NotNull CommandExecutionException additionalMessage(final @NotNull String message,
                                                                final @NotNull Argument @NotNull ... arguments) {
        additionalMessages.put(message, arguments);
        return this;
    }

    /**
     * Adds new arguments to the final message.
     *
     * @param arguments the arguments
     * @return this object (for method chaining)
     */
    public @NotNull CommandExecutionException arguments(final @NotNull Argument @NotNull ... arguments) {
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
