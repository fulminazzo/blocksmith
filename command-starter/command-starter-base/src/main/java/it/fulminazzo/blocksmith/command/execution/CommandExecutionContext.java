package it.fulminazzo.blocksmith.command.execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the execution context of a command.
 */
@RequiredArgsConstructor
public final class CommandExecutionContext {
    @Getter
    private final @NotNull Object commandSender;
    private final @NotNull List<String> input = new ArrayList<>();
    @Getter
    private final @NotNull LinkedList<Object> arguments = new LinkedList<>();
    private int current;

    /**
     * Adds more data to the input.
     *
     * @param input the input
     * @return this object (for method chaining)
     */
    public @NotNull CommandExecutionContext addInput(final String @NotNull ... input) {
        this.input.addAll(Arrays.asList(input));
        return this;
    }

    /**
     * Gets the current argument from the input.
     *
     * @return the current argument
     */
    public @NotNull String getCurrent() {
        return input.get(current);
    }

    /**
     * Checks if the cursor has reached the final list of arguments.
     *
     * @return <code>true</code> if it has
     */
    public boolean isDone() {
        return current >= input.size();
    }

    /**
     * Advances the internal cursor to the next argument.
     *
     * @return this object (for method chaining)
     */
    public @NotNull CommandExecutionContext advanceCursor() {
        current++;
        return this;
    }

    /**
     * Adds a new parsed argument to the internal pool.
     *
     * @param argument the argument
     */
    public void addParsedArgument(final @Nullable Object argument) {
        arguments.add(argument);
    }

}
