package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.annotation.Confirm;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.structure.task.PendingTaskManager;
import org.jetbrains.annotations.NotNull;

/**
 * A special {@link CommandNode} to confirm the execution of a command.
 */
public final class ConfirmNode extends ConfirmationNode {

    /**
     * Instantiates a new Confirm node.
     *
     * @param confirmationAnnotation the {@link Confirm} annotation to get information from
     * @param parent                 the parent node
     * @param confirmationManager    the confirmation manager
     */
    public ConfirmNode(final @NotNull Confirm confirmationAnnotation,
                       final @NotNull LiteralNode parent,
                       final @NotNull PendingTaskManager<Object> confirmationManager) {
        super(
                confirmationAnnotation.confirmAliases(),
                confirmationAnnotation.confirmDescription(),
                confirmationAnnotation.confirmPermission(),
                parent
        );
        setExecutor((n, v) -> {
            final Object id = v.getCommandSender().getId();
            final PendingTaskManager.Result result = confirmationManager.execute(id);
            if (result == PendingTaskManager.Result.EXPIRED)
                throw new CommandExecutionException(CommandMessages.PENDING_ACTION_EXPIRED);
            else if (result == PendingTaskManager.Result.NOT_FOUND)
                throw new CommandExecutionException(CommandMessages.NO_PENDING_ACTION);
        });
    }

}
