package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.conversion.Convertible;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Identifies an application bound to a server.
 */
public interface ServerApplication extends Convertible {

    /**
     * Gets the server that the application is running on.
     *
     * @param <S> the type of the server
     * @return the server
     */
    <S> @NotNull S server();

    /**
     * Gets the logger associated with the application.
     *
     * @return the logger
     */
    @NotNull Logger logger();

    /**
     * Gets the name of the application in lower case.
     *
     * @return the name
     */
    default @NotNull String lowercaseName() {
        return getName().toLowerCase();
    }

    /**
     * Gets the name of the application.
     *
     * @return the name
     */
    @NotNull String getName();

}
