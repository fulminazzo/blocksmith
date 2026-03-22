package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A parser to convert a raw string into a Java type.
 *
 * @param <T> the type of the parsed argument
 */
public interface ArgumentParser<T> {

    /**
     * Parses the given string
     *
     * @param rawArgument the raw string
     * @return the object
     * @throws CommandExecutionException in case of parsing errors
     */
    @Nullable T parse(final @NotNull String rawArgument) throws CommandExecutionException;

}
