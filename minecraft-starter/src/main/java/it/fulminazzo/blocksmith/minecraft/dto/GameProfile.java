package it.fulminazzo.blocksmith.minecraft.dto;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Identifies the profile of a player.
 */
@Value
public class GameProfile {
    @NotNull UUID uuid;
    @NotNull String name;

    @Nullable SkinData skin;

}
