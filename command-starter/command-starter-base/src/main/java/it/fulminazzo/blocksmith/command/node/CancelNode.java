package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.annotation.Confirm;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.structure.task.PendingTaskManager;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * A special {@link CommandNode} to cancel the execution of a command.
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class CancelNode extends ConfirmationNode {

    /**
     * Instantiates a new Cancel node.
     *
     * @param confirmationAnnotation the {@link Confirm} annotation to get information from
     * @param parent                 the parent node
     * @param confirmationManager    the confirmation manager
     */
    public CancelNode(final @NotNull Confirm confirmationAnnotation,
                      final @NotNull LiteralNode parent,
                      final @NotNull PendingTaskManager<Object> confirmationManager) {
        super(
                confirmationAnnotation.cancelAliases(),
                confirmationAnnotation.cancelDescription(),
                confirmationAnnotation.cancelPermission(),
                parent
        );
        setExecutor((n, v) -> {
            final Object id = v.getCommandSender().getId();
            final PendingTaskManager.Result result = confirmationManager.cancel(id);
            if (result == PendingTaskManager.Result.SUCCESS)
                throw new CommandExecutionException(CommandMessages.PENDING_ACTION_CANCELLED);
            if (result == PendingTaskManager.Result.EXPIRED)
                throw new CommandExecutionException(CommandMessages.PENDING_ACTION_EXPIRED);
            else if (result == PendingTaskManager.Result.NOT_FOUND)
                throw new CommandExecutionException(CommandMessages.NO_PENDING_ACTION);
        });
    }

}
