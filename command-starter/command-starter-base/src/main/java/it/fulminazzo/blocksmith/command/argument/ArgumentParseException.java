package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.message.argument.Argument;
import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An exception thrown by {@link ArgumentParser} in case of parsing exceptions.
 */
@StandardException
public final class ArgumentParseException extends Exception {
    private static final long serialVersionUID = 3979898718778768057L;

    private final @NotNull List<Argument> arguments = new ArrayList<>();

    /**
     * Adds new arguments to the final message.
     *
     * @param arguments the arguments
     * @return this object (for method chaining)
     */
    public @NotNull ArgumentParseException arguments(final @NotNull Argument @NotNull ... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
        return this;
    }

    /**
     * Get the arguments.
     *
     * @return the arguments
     */
    public @NotNull Argument @NotNull [] getArguments() {
        return arguments.toArray(new Argument[0]);
    }

}
