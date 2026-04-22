package it.fulminazzo.blocksmith.command.visitor.usage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Holds the universal styling for the usage of {@link it.fulminazzo.blocksmith.command.node.CommandNode}s.
 * <br>
 * Each color follows the <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>.
 */
@Getter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UsageStyle {
    private static @NotNull UsageStyle instance = new UsageStyle();

    /*
     * LITERAL
     */
    private @NotNull String literalColor = "red";
    private @NotNull String literalSeparator = "|";
    private @NotNull String literalSeparatorColor = "dark_gray";

    /*
     * LITERAL
     */

    /**
     * Sets the color for displaying the aliases of a {@link it.fulminazzo.blocksmith.command.node.LiteralNode}.
     *
     * @param color the color (parsed through the <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle literalColor(final @NotNull String color) {
        this.literalColor = color;
        return this;
    }

    /**
     * Sets the separator between the aliases of a {@link it.fulminazzo.blocksmith.command.node.LiteralNode}.
     *
     * @param separator the separator
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle literalSeparator(final @NotNull String separator) {
        this.literalSeparator = separator;
        return this;
    }

    /**
     * Sets the color of the separator between the aliases of a {@link it.fulminazzo.blocksmith.command.node.LiteralNode}.
     *
     * @param color the color (parsed through the <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle literalSeparatorColor(final @NotNull String color) {
        this.literalSeparatorColor = color;
        return this;
    }


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
