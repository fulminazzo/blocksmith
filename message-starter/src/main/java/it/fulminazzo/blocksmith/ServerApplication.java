package it.fulminazzo.blocksmith;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Identifies an application bound to a server.
 */
public interface ServerApplication {

    /**
     * Gets the server that the application is running on.
     *
     * @param <S> the type of the server
     * @return the server
     */
    <S> @NotNull S server();

    /**
     * Returns the application as an instance of the given type.
     *
     * @param <T>  the type of the cast
     * @param type the type
     * @return the cast application
     */
    <T> @NotNull T as(final @NotNull Class<T> type);

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
