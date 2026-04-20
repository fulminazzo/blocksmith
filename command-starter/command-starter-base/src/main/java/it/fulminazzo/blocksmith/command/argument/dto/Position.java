package it.fulminazzo.blocksmith.command.argument.dto;

import it.fulminazzo.blocksmith.conversion.Convertible;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a general position in the Minecraft coordinate system.
 */
@Value
public class Position implements Convertible {
    @NotNull Coordinate x;
    @NotNull Coordinate y;
    @NotNull Coordinate z;

}
