package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.command.CommandRegistry;
import it.fulminazzo.blocksmith.message.Messenger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

/**
 * Provides a handle to the underlying application's utilities and metadata.
 */
public interface ApplicationHandle extends ServerApplication {

    /**
     * Gets the registry responsible for handling and registering commands.
     *
     * @return the command registry
     */
    @NotNull CommandRegistry getCommandRegistry();

    /**
     * Gets the handler for messages sending.
     *
     * @return the messenger
     */
    @NotNull Messenger getMessenger();

    /**
     * Gets an executor responsible for handling asynchronous tasks.
     *
     * @return the executor
     */
    @NotNull ExecutorService getExecutor();

}
