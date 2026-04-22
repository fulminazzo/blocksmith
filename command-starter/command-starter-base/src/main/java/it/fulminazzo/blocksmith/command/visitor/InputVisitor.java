package it.fulminazzo.blocksmith.command.visitor;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * A special kind of {@link Visitor} that holds the input of the user.
 *
 * @param <T> the type of the result
 * @param <X> the type of the exception to throw in case of errors during visits
 */
public interface InputVisitor<T, X extends Exception> extends Visitor<T, X> {

    /**
     * Gets the actual input.
     *
     * @return the input
     */
    @NotNull CommandInput getInput();

    /**
     * Gets the user that sent the input.
     *
     * @return the user
     */
    @NotNull CommandSenderWrapper<?> getCommandSender();

    /**
     * Gets the application that created this visitor.
     *
     * @return the application
     */
    @NotNull ApplicationHandle getApplication();

}
