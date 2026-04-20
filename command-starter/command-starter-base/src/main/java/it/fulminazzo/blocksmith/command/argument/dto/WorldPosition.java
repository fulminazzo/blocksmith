package it.fulminazzo.blocksmith.command.argument.dto;

import it.fulminazzo.blocksmith.conversion.Convertible;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a position in the world.
 */
@Value
public class WorldPosition implements Convertible {
    @NotNull String world;
    @NotNull Coordinate x;
    @NotNull Coordinate y;
    @NotNull Coordinate z;

}
