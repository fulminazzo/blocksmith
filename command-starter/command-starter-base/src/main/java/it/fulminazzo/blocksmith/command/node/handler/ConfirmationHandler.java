package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.TabCompletable;
import it.fulminazzo.blocksmith.command.annotation.Confirm;
import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor;
import it.fulminazzo.blocksmith.function.RunnableException;
import it.fulminazzo.blocksmith.structure.task.PendingTaskManager;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Handles any required confirmation for commands execution.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ConfirmationHandler implements TabCompletable {
    @NotNull Confirm confirmationInfo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotNull PendingTaskManager<Object> confirmationManager = new PendingTaskManager<>();

    /**
     * Handles the execution of the given function, registering a confirmation task
     * for the sender to confirm.
     *
     * @param visitor         the visitor
     * @param executeFunction the function to execute
     */
    public void handleExecution(final @NotNull CommandExecutionVisitor visitor,
                                final @NotNull RunnableException<CommandExecutionException> executeFunction) {
        confirmationManager.register(
                visitor.getCommandSender().getId(),
                getConfirmationTimeout(),
                () -> {
                    try {
                        executeFunction.run();
                    } catch (CommandExecutionException e) {
                        visitor.handleCommandExecutionException(e);
                    }
                }
        );
    }

    /**
     * Checks the current {@link CommandInput} and, if it's a confirmation request, executes the corresponding action.
     *
     * @param visitor the visitor
     * @return {@code true} if the next argument was either {@link Confirm#confirmWord()}
     * or {@link Confirm#cancelWord()} and the execution was successful,
     * {@code false} otherwise
     * @throws CommandExecutionException if the execution failed for any reason (e.g., the action was not found)
     */
    public boolean checkConfirmationKeywords(final @NotNull CommandExecutionVisitor visitor) throws CommandExecutionException {
        final CommandInput input = visitor.getInput();
        if (input.isLast()) return false;
        String argument = input.peek();

        final Object id = visitor.getCommandSender().getId();

        final PendingTaskManager.Result result;
        if (argument.equalsIgnoreCase(getConfirmWord())) {
            result = confirmationManager.execute(id);
            if (result == PendingTaskManager.Result.SUCCESS) return true;
        } else if (argument.equalsIgnoreCase(getCancelWord())) {
            result = confirmationManager.cancel(id);
            if (result == PendingTaskManager.Result.SUCCESS)
                throw new CommandExecutionException("success.pending-action-cancelled");
        } else return false;

        if (result == PendingTaskManager.Result.NOT_FOUND)
            throw new CommandExecutionException("error.no-pending-action");
        else
            // should only be expired at this point
            throw new CommandExecutionException("error.pending-action-expired");
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        return Arrays.asList(getConfirmWord(), getCancelWord());
    }

    /**
     * Gets the literal required to confirm the execution.
     *
     * @return the confirmation word
     */
    public @NotNull String getConfirmWord() {
        return confirmationInfo.confirmWord();
    }

    /**
     * Gets the literal required to cancel the execution.
     *
     * @return the cancellation word
     */
    public @NotNull String getCancelWord() {
        return confirmationInfo.cancelWord();
    }

    /**
     * Gets the confirmation timeout.
     *
     * @return the confirmation timeout
     */
    public @NotNull Duration getConfirmationTimeout() {
        return Duration.of(confirmationInfo.timeout(), confirmationInfo.unit().toChronoUnit());
    }

}
