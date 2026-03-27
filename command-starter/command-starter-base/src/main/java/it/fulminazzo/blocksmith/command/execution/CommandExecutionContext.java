package it.fulminazzo.blocksmith.command.execution;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.CommandRegistry;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the execution context of a command.
 */
@ToString
@RequiredArgsConstructor
public final class CommandExecutionContext {
    @Getter
    private final @NotNull ApplicationHandle application;
    @Getter
    private final @NotNull CommandRegistry registry;
    @Getter
    private final @NotNull CommandSenderWrapper<?> commandSender;
    @Getter
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
        return addInput(Arrays.asList(input));
    }

    /**
     * Adds more data to the input.
     *
     * @param input the input
     * @return this object (for method chaining)
     */
    public @NotNull CommandExecutionContext addInput(final @NotNull Collection<String> input) {
        this.input.addAll(input);
        return this;
    }

    /**
     * Merges all the remaining input into one space separated argument (for greedy arguments).
     */
    public void mergeRemainingInput() {
        StringBuilder argument = new StringBuilder(getCurrent());
        while (input.size() > current + 1)
            argument.append(" ").append(input.remove(current + 1));
        input.set(current, argument.toString());
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
     * Gets the last argument from the input.
     *
     * @return the last argument
     */
    public @NotNull String getLast() {
        return input.get(input.size() - 1);
    }

    /**
     * Checks if the cursor has reached the final argument.
     *
     * @return <code>true</code> if it has
     */
    public boolean isLast() {
        return current == input.size() - 1;
    }

    /**
     * Checks if the cursor has reached read all the available arguments.
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
