package it.fulminazzo.blocksmith.command.visitor.execution;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.NumberArgumentNode;
import it.fulminazzo.blocksmith.command.visitor.VisitorImpl;
import it.fulminazzo.blocksmith.message.argument.Argument;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * A special {@link it.fulminazzo.blocksmith.command.visitor.Visitor} handling command execution.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandExecutionVisitor extends VisitorImpl<Void> {
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

    @Override
    protected Void visitCommandNode(final @NotNull CommandNode node) {
        throw new UnsupportedOperationException(); //TODO: implement
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
     * Handles a {@link CommandExecutionException} accordingly
     *
     * @param exception the exception
     */
    public void handleCommandExecutionException(final @NotNull CommandExecutionException exception) {
        String message = exception.getMessage();
        if (message.isEmpty()) return;
        application.getMessenger().sendMessage(commandSender, message, getArguments(exception));
        Throwable cause = exception.getCause();
        if (cause != null)
            application.getLog().warn("{} while executing command /{}",
                    cause.getClass().getCanonicalName(),
                    input.getRawInput(),
                    cause
            );
    }

    private @NotNull Argument[] getArguments(final @NotNull CommandExecutionException exception) {
        Argument[] previous = exception.getArguments();
        Argument[] args = Arrays.copyOf(previous, previous.length + 1);
        args[previous.length] = Placeholder.of("input", input.getRawInput());
        return args;
    }

}
