package it.fulminazzo.blocksmith.command.execution;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.PermissionInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Represents the execution context of a command.
 *
 * @param <S> the type of the sender
 */
@ToString
@RequiredArgsConstructor
public final class CommandExecutionContext<S> {
    @Getter
    private final @NotNull S commandSender;
    private final @NotNull BiPredicate<S, PermissionInfo> permissionChecker;
    private final @NotNull List<String> input = new ArrayList<>();
    @Getter
    private final @NotNull LinkedList<Object> arguments = new LinkedList<>();
    private int current;

    /**
     * Checks if the internal sender has the permission.
     *
     * @param permission the permission
     * @return <code>true</code> if it does
     */
    public boolean hasPermission(final @NotNull PermissionInfo permission) {
        return permission.getPermissionDefault() == Permission.Default.ALL ||
                permissionChecker.test(commandSender, permission);
    }

    /**
     * Adds more data to the input.
     *
     * @param input the input
     * @return this object (for method chaining)
     */
    public @NotNull CommandExecutionContext<S> addInput(final String @NotNull ... input) {
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
    public @NotNull CommandExecutionContext<S> advanceCursor() {
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
