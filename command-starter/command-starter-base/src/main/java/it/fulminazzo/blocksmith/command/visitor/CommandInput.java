package it.fulminazzo.blocksmith.command.visitor;

import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Holds the input of a user upon execution of a command.
 */
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public final class CommandInput {
    final @NotNull List<String> input = new ArrayList<>();

    int current;

    /**
     * Merges all the remaining input into one space-separated argument (for greedy arguments).
     *
     * @return the merged input
     */
    public @NotNull String mergeRemaining() {
        StringBuilder argument = new StringBuilder(getCurrent());
        while (input.size() > current + 1)
            argument.append(" ").append(input.remove(current + 1));
        String string = argument.toString();
        input.set(current, string);
        return string;
    }

    /**
     * Adds more data to the input.
     *
     * @param input the input
     * @return this object (for method chaining)
     */
    public @NotNull CommandInput addInput(final @NotNull String @NotNull ... input) {
        return addInput(Arrays.asList(input));
    }

    /**
     * Adds more data to the input.
     *
     * @param input the input
     * @return this object (for method chaining)
     */
    public @NotNull CommandInput addInput(final @NotNull Collection<String> input) {
        this.input.addAll(input);
        return this;
    }

    /*
     * SETTERS
     */

    /**
     * Advances the internal cursor to the next argument.
     *
     * @return this object (for method chaining)
     */
    public @NotNull CommandInput advanceCursor() {
        current++;
        return this;
    }

    /**
     * Injects a new argument into the current position of the input.
     *
     * @param input the argument
     * @return this object (for method chaining)
     */
    public @NotNull CommandInput setCurrent(final @NotNull String input) {
        this.input.set(current, input);
        return this;
    }

    /*
     * GETTERS
     */

    /**
     * Gets the current argument from the input.
     *
     * @return the current argument
     */
    public @NotNull String getCurrent() {
        return input.get(current);
    }

    /**
     * Gets the next argument from the input (without advancing the cursor).
     *
     * @return the next argument
     */
    public @NotNull String peek() {
        return input.get(current + 1);
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

}
