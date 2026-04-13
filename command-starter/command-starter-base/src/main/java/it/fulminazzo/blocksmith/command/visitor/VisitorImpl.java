package it.fulminazzo.blocksmith.command.visitor;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation of {@link Visitor} with internal information.
 *
 * @param <T> the type of the result
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class VisitorImpl<T> implements Visitor<T> {
    @NotNull ApplicationHandle application;
    @NotNull CommandSenderWrapper<?> commandSender;

    @NotNull CommandInput input;

    /**
     * Instantiates a new Visitor.
     *
     * @param application   the application
     * @param commandSender the command sender
     * @param commandName   the command name
     * @param arguments     the arguments
     */
    protected VisitorImpl(final @NotNull ApplicationHandle application,
                          final @NotNull CommandSenderWrapper<?> commandSender,
                          final @NotNull String commandName,
                          final @NotNull String @NotNull ... arguments) {
        this.application = application;
        this.commandSender = commandSender;

        this.input = new CommandInput().addInput(commandName).addInput(arguments);
    }

}
