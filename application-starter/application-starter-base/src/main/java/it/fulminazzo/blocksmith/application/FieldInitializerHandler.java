package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface for handling the logic of field initializers.
 *
 * @see Application
 * @see Initializer
 * @see ApplicationExtensionRegistry
 */
@FunctionalInterface
public interface FieldInitializerHandler {

    /**
     * Executes the initialization logic for the given field.
     *
     * @param context   the context where the handler is running
     * @param fieldData the data of the field to initialize
     */
    void handle(
            final @NotNull InitializationContext context,
            final @NotNull FieldData fieldData
    );

}
