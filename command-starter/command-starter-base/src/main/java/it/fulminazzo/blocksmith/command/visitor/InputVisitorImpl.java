package it.fulminazzo.blocksmith.command.visitor;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation of {@link InputVisitor} with internal information.
 *
 * @param <T> the type of the result
 * @param <X> the type of the exception to throw in case of errors during visits
 */
@Getter
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class InputVisitorImpl<T, X extends Exception> implements InputVisitor<T, X> {
    @NotNull ApplicationHandle application;
    @NotNull CommandSenderWrapper<?> commandSender;

    @NotNull CommandInput input;

    /**
     * Instantiates a new Visitor.
     *
     * @param application   the application
     * @param commandSender the command sender
     * @param commandName   the name of the command
     * @param arguments     the arguments given to the command
     */
    protected InputVisitorImpl(final @NotNull ApplicationHandle application,
                               final @NotNull CommandSenderWrapper<?> commandSender,
                               final @NotNull String commandName,
                               final @NotNull String @NotNull ... arguments) {
        this.application = application;
        this.commandSender = commandSender;

        this.input = new CommandInput().addInput(commandName).addInput(arguments);
    }

    /**
     * Visits a general {@link CommandNode}.
     * Should support visits of implementations.
     *
     * @param node the node
     * @return the result
     * @throws X the exception thrown in case of visit errors
     */
    protected abstract T visitCommandNode(final @NotNull CommandNode node) throws X;

}
