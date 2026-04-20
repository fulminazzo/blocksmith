package it.fulminazzo.blocksmith.command.argument.dto;

import lombok.Value;

/**
 * Represents a coordinate in the Minecraft convention system.
 * <br>
 * If the coordinate is relative, it will be prefixed with a tilde (~).
 */
@Value
public class Coordinate {
    /**
     * Identifies a relative coordinate.
     */
    public static final String RELATIVE_IDENTIFIER = "~";

    double value;
    boolean relative;

    @Override
    public String toString() {
        return String.format("%s%s", relative ? RELATIVE_IDENTIFIER : "", value);
    }

}
