package it.fulminazzo.blocksmith.command.visitor.execution;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.NumberArgumentNode;
import it.fulminazzo.blocksmith.command.visitor.VisitorImpl;
import org.jetbrains.annotations.NotNull;

/**
 * A special {@link it.fulminazzo.blocksmith.command.visitor.Visitor} handling command execution.
 */
public final class CommandExecutionVisitor extends VisitorImpl<Void> {
    private final @NotNull ExecutionContext context;

    /**
     * Instantiates a new Command execution visitor.
     *
     * @param application   the application
     * @param commandSender the command sender
     * @param commandName   the command name
     * @param arguments     the arguments
     */
    public CommandExecutionVisitor(final @NotNull ApplicationHandle application,
                                   final @NotNull CommandSenderWrapper<?> commandSender,
                                   final @NotNull String commandName,
                                   final @NotNull String @NotNull ... arguments) {
        super(application, commandSender, commandName, arguments);
        this.context = new ExecutionContext(commandSender); //TODO: repetitive, useless
    }

    @Override
    public Void visitArgumentNode(final @NotNull ArgumentNode<?> node) {
        throw new UnsupportedOperationException(); //TODO: implement
    }

    @Override
    public Void visitNumberArgumentNode(final @NotNull NumberArgumentNode<?> node) {
        throw new UnsupportedOperationException(); //TODO: implement
    }

    @Override
    public Void visitLiteralNode(final @NotNull LiteralNode node) {
        throw new UnsupportedOperationException(); //TODO: implement
    }

}
