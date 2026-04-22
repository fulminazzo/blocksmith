package it.fulminazzo.blocksmith.command.visitor.usage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Holds the universal styling for the usage of commands.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UsageStyle {
    private static @NotNull UsageStyle instance = new UsageStyle();

    /**
     * Resets the styling to its default values.
     *
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle defaults() {
        instance = new UsageStyle();
        return this;
    }

    /**
     * Get the usage style.
     *
     * @return the usage style
     */
    public static @NotNull UsageStyle get() {
        return instance;
    }

}
