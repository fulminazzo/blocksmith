package it.fulminazzo.blocksmith.reflect;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Mock class for testing purposes.
 *
 * @see ReflectFunctionalTest
 */
@SuppressWarnings("unused")
public interface Entity {
    @NotNull String ENTITIES_DEFAULT_NAME = "Steve";

    /**
     * Gets unique id.
     *
     * @return the unique id
     */
    default @NotNull UUID getUniqueId() {
        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    @NotNull String getName();

}
