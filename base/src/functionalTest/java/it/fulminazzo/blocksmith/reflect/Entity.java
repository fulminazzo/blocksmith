package it.fulminazzo.blocksmith.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
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
        String name = getName();
        if (name == null) throw new IllegalStateException("Entity has no name");
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    @Nullable String getName();

}
