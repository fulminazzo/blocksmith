package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.annotation.Confirm;
import it.fulminazzo.blocksmith.structure.task.PendingTaskManager;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * Handles any required confirmation for commands execution.
 */
@Value
public class ConfirmationHandler {
    @NotNull Confirm confirmationInfo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotNull PendingTaskManager<Object> confirmationManager = new PendingTaskManager<>();

    //TODO: handle execute
    //TODO: handle tab completion

}
