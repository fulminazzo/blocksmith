package it.fulminazzo.blocksmith.command.argument.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Represents a coordinate in the Minecraft convention system.
 * <br>
 * If the coordinate is relative, it will be prefixed with a tilde (~).
 */
@Value
@AllArgsConstructor
public class Coordinate {
    /**
     * Identifies a relative coordinate.
     */
    public static final String RELATIVE_IDENTIFIER = "~";

    double value;
    boolean relative;

    /**
     * Instantiates a new Coordinate.
     *
     * @param value the value
     */
    public Coordinate(double value) {
        this(value, false);
    }

    @Override
    public String toString() {
        return String.format("%s%s", relative ? RELATIVE_IDENTIFIER : "", value);
    }

}
