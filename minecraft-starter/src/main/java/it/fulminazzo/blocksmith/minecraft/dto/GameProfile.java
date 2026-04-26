package it.fulminazzo.blocksmith.minecraft.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Identifies the profile of a player.
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class GameProfile {
    final @NotNull UUID uuid;
    final @NotNull String name;

    @Nullable SkinData skin;

}
