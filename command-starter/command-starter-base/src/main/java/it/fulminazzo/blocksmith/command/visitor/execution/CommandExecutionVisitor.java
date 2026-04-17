package it.fulminazzo.blocksmith.command.visitor.execution;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.handler.ConfirmationHandler;
import it.fulminazzo.blocksmith.command.node.handler.ExecutionHandler;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import it.fulminazzo.blocksmith.command.visitor.VisitorImpl;
import it.fulminazzo.blocksmith.message.argument.Argument;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.argument.Time;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

/**
 * A special {@link it.fulminazzo.blocksmith.command.visitor.Visitor} handling command execution.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class CommandExecutionVisitor extends VisitorImpl<Void, CommandExecutionException> {
    @Getter
    @NotNull LinkedList<Object> arguments = new LinkedList<>();

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
    }

    @Override
    public Void visitArgumentNode(final @NotNull ArgumentNode<?> node) throws CommandExecutionException {
        try {
            Object argument = node.parseCurrent(this);
            addArgument(argument);
            return visitCommandNode(node);
        } catch (ArgumentParseException e) {
            throw new CommandExecutionException(e.getMessage(), e.getCause()).arguments(e.getArguments());
        }
    }

    @Override
    public Void visitLiteralNode(final @NotNull LiteralNode node) throws CommandExecutionException {
        PermissionInfo permission = node.getCommandInfo().getPermission();
        if (!commandSender.hasPermission(permission))
            throw new CommandExecutionException("error.no-permission")
                    .arguments(Placeholder.of("permission", permission.getPermission()));
        ConfirmationHandler confirmationHandler = node.getConfirmationHandler();
        if (confirmationHandler != null && confirmationHandler.checkConfirmationKeywords(this)) return null;
        return visitCommandNode(node);
    }

    @Override
    protected Void visitCommandNode(final @NotNull CommandNode node) throws CommandExecutionException {
        if (input.advanceCursor().isDone()) {
            if (node.isExecutable()) return handleExecution(node);
            else {
                ArgumentNode<?> optional = node.getOptionalArgument();
                if (optional != null) return optional.accept(this);
                else throw new CommandExecutionException("error.not-enough-arguments");
            }
        } else {
            final String current = input.getCurrent();
            CommandNode child = node.getChild(current);
            if (child == null) {
                if (node.isExecutable()) return handleExecution(node);
                else throw new CommandExecutionException("error.command-not-found")
                        .arguments(Placeholder.of("argument", current));
            } else return child.accept(this);
        }
    }

    /**
     * Adds a new argument to the internal pool.
     *
     * @param argument the argument
     * @return this object (for method chaining)
     */
    public @NotNull CommandExecutionVisitor addArgument(final @Nullable Object argument) {
        arguments.add(argument);
        return this;
    }

    /**
     * Handles a {@link CommandExecutionException} accordingly.
     *
     * @param exception the exception
     */
    public void handleCommandExecutionException(final @NotNull CommandExecutionException exception) {
        String message = exception.getMessage();
        application.getMessenger().sendMessage(commandSender, message, getArguments(exception));
        Throwable cause = exception.getCause();
        if (cause != null)
            application.getLog().warn("{} while executing command /{}",
                    cause.getClass().getCanonicalName(),
                    input.getRawInput(),
                    cause
            );
    }

    /**
     * Handles the execution of the given node accordingly.
     *
     * @param node the node
     * @return <code>null</code>
     * @throws CommandExecutionException if a confirmation is required or the execution failed
     */
    Void handleExecution(final @NotNull CommandNode node) throws CommandExecutionException {
        final ExecutionHandler executionHandler = node.getExecutor()
                .orElseThrow(() -> new IllegalStateException("No execution handler found for node: " + node));
        LiteralNode commandNode = Objects.requireNonNull(node.getCommandNode(), "Could not find command node of node: " + node);
        ConfirmationHandler confirmationHandler = commandNode.getConfirmationHandler();
        if (confirmationHandler != null) {
            confirmationHandler.handleExecution(
                    this,
                    () -> executionHandler.execute(commandNode, this)
            );
            throw new CommandExecutionException("general.await-confirmation")
                    .arguments(Time.of("time", confirmationHandler.getConfirmationTimeout().toMillis()));
        }
        executionHandler.execute(commandNode, this);
        return null;
    }

    private @NotNull Argument[] getArguments(final @NotNull CommandExecutionException exception) {
        Argument[] previous = exception.getArguments();
        Argument[] args = Arrays.copyOf(previous, previous.length + 1);
        args[previous.length] = Placeholder.of("input", input.getRawInput());
        return args;
    }

}
