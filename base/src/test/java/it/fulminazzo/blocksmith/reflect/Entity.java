package it.fulminazzo.blocksmith.reflect;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Entity {
    @NotNull String ENTITIES_DEFAULT_NAME = "Steve";

    default @NotNull UUID getUniqueId() {
        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @NotNull String getName();

}
