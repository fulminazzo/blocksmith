package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.annotation.Confirm;
import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.structure.task.PendingTaskManager;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Handles any required confirmation for commands execution.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ConfirmationHandler {
    @NotNull Confirm confirmationInfo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotNull PendingTaskManager<Object> confirmationManager = new PendingTaskManager<>();

    /**
     * Checks the current {@link CommandInput} and, if it's a confirmation request, executes the corresponding action.
     *
     * @param visitor the visitor
     * @return <code>true</code> if the next argument was either {@link Confirm#confirmWord()} or {@link Confirm#cancelWord()}
     * and the execution was successful, <code>false</code> otherwise
     * @throws CommandExecutionException if the execution failed for any reason (e.g. the action was not found)
     */
    public boolean handleExecution(final @NotNull Visitor<?, ?> visitor) throws CommandExecutionException {
        final CommandInput input = visitor.getInput();
        if (input.isLast()) return false;
        String argument = input.peek();

        final Object id = visitor.getCommandSender();

        final PendingTaskManager.Result result;
        if (argument.equalsIgnoreCase(confirmationInfo.confirmWord())) {
            //TODO: what about CommandExecutionException during execution?
            result = confirmationManager.execute(id);
            if (result == PendingTaskManager.Result.SUCCESS) return true;
        } else if (argument.equalsIgnoreCase(confirmationInfo.cancelWord())) {
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

    //TODO: handle tab completion

}
