package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Handler to actually execute the command upon successful argument validation.
 */
@FunctionalInterface
public interface IExecutionHandler {

    /**
     * Executes the actual command logic.
     *
     * @param commandNode      the literal node containing information about the executing command
     * @param executionVisitor the execution visitor
     * @throws CommandExecutionException in case of any errors
     */
    void execute(final @NotNull LiteralNode commandNode,
                 final @NotNull CommandExecutionVisitor executionVisitor) throws CommandExecutionException;

}
