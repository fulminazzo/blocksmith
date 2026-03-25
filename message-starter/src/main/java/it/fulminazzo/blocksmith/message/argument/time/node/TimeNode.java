package it.fulminazzo.blocksmith.message.argument.time.node;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the node of a time format, whether it is static or dynamic.
 */
@EqualsAndHashCode
@ToString
public abstract class TimeNode {
    @Getter
    @Setter
    private @Nullable TimeNode child;

    /**
     * Parses the given time into this time format.
     *
     * @param time the time (in milliseconds)
     * @return the formatted time
     */
    public @NotNull String parse(final long time) {
        String parsed = parseSingle(time);
        if (child != null) parsed = parsed + child.parse(time);
        return parsed;
    }

    /**
     * Parses the given time into this node format (the result will not include any child).
     *
     * @param time the time (in milliseconds)
     * @return the formatted time
     */
    protected abstract @NotNull String parseSingle(final long time);

}
