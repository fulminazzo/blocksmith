package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.message.Messenger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Represents a blocksmith application.
 */
public interface BlocksmithApplication {

    /**
     * Gets the handler for messages sending.
     *
     * @return the messenger
     */
    @NotNull Messenger getMessenger();

    /**
     * Gets the SLF4J logger.
     *
     * @return the logger
     */
    @NotNull Logger getLog();

    /**
     * Gets the server where the application has been started.
     *
     * @return the server
     */
    @NotNull Object getServer();

    /**
     * Gets the name of this application.
     *
     * @return the name
     */
    @NotNull String getName();

}
