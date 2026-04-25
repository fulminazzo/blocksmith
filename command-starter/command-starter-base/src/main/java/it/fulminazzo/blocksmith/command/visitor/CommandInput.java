package it.fulminazzo.blocksmith.command.visitor;

import it.fulminazzo.blocksmith.util.StringUtils;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds the input of a user upon execution of a command.
 */
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@ToString(doNotUseGetters = true)
public final class CommandInput {
    private static final @NotNull String DELIMITER = " ";
    private static final @NotNull String @NotNull [] QUOTES = new String[]{"\"", "'"};

    final @NotNull List<String> input = new ArrayList<>();

    int current;

    /**
     * Creates a copy of the current command input (for later restore).
     *
     * @return the command input
     */
    public @NotNull CommandInput snapshot() {
        CommandInput snapshot = new CommandInput();
        snapshot.input.addAll(input);
        snapshot.current = current;
        return snapshot;
    }

    /**
     * Restores the input and current cursor from the given snapshot.
     *
     * @param snapshot the snapshot
     */
    public void restore(final @NotNull CommandInput snapshot) {
        input.clear();
        input.addAll(snapshot.input);
        current = snapshot.current;
    }

    /**
     * Merges all the remaining input into one space-separated argument (for greedy arguments).
     */
    public void mergeRemaining() {
        if (isDone()) return;
        StringBuilder argument = new StringBuilder(getCurrent());
        while (input.size() > current + 1)
            argument.append(DELIMITER).append(input.remove(current + 1));
        input.set(current, argument.toString());
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
        if (input.isEmpty()) return this;
        String current = getRawInput();
        if (!current.isEmpty()) current += DELIMITER;
        String rawInput = String.join(DELIMITER, input);
        current += rawInput;
        this.input.clear();
        this.input.addAll(StringUtils.split(current, DELIMITER, true, "'", "\""));
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
     * Retrocedes the internal cursor to the previous argument.
     *
     * @return this object (for method chaining)
     */
    public @NotNull CommandInput retrocedeCursor() {
        current--;
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
        return unquote(input.get(current));
    }

    /**
     * Gets the next argument from the input (without advancing the cursor).
     *
     * @return the next argument
     */
    public @NotNull String peek() {
        return unquote(input.get(current + 1));
    }

    /**
     * Checks if the cursor has reached the final argument.
     *
     * @return {@code true} if it has
     */
    public boolean isLast() {
        return current == input.size() - 1;
    }

    /**
     * Checks if the cursor has reached read all the available arguments.
     *
     * @return {@code true} if it has
     */
    public boolean isDone() {
        return current >= input.size();
    }

    /**
     * Gets the input up until the cursor, space-separated.
     *
     * @return the input
     */
    public @NotNull String getPartialRawInput() {
        return String.join(DELIMITER, input.subList(0, Math.min(input.size(), current)));
    }

    /**
     * Gets the input, space-separated.
     *
     * @return the input
     */
    public @NotNull String getRawInput() {
        return String.join(DELIMITER, input);
    }

    /**
     * Gets the total input.
     *
     * @return the input
     */
    public @NotNull List<String> getInput() {
        return input.stream().map(CommandInput::unquote).collect(Collectors.toUnmodifiableList());
    }

    private static @NotNull String unquote(final @NotNull String string) {
        for (String q : QUOTES)
            if (string.startsWith(q) && string.endsWith(q) && string.length() > 1)
                return string.substring(1, string.length() - 1);
        return string;
    }

}
