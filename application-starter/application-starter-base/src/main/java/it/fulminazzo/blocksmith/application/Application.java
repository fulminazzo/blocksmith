package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

/**
 * The basic entry point of a program. Contains useful data and basic functioning information.
 *
 * @see ApplicationHandlers
 */
public interface Application {

    /**
     * Gets the directory where the application should operate.
     *
     * @return the directory
     */
    @NotNull File directory();

    /**
     * Gets the logger associated with the application.
     *
     * @return the logger
     */
    @NotNull Logger logger();

}
