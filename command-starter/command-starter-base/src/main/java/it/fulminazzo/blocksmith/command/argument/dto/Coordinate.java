package it.fulminazzo.blocksmith.command.argument.dto;

import lombok.Value;

/**
 * Represents a coordinate in the Minecraft convention system.
 * <br>
 * If the coordinate is relative, it will be prefixed with a tilde (~).
 */
@Value
public class Coordinate {
    double value;
    boolean relative;

    @Override
    public String toString() {
        return String.format("%s%s", relative ? "~" : "", value);
    }

}
