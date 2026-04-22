package it.fulminazzo.blocksmith.command.visitor.usage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * COMMON
     */

    private @NotNull String separator = "|";
    private @NotNull String punctuationColor = "dark_gray";

    /*
     * LITERAL
     */

    private @NotNull String literalColor = "red";
    private @NotNull String literalSeparator = "|";
    private @Nullable String literalSeparatorColor;

    /*
     * COMMON
     */

    /**
     * Sets the separator between the children of a {@link it.fulminazzo.blocksmith.command.node.CommandNode}.
     *
     * @param separator the separator
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle separator(final @NotNull String separator) {
        this.separator = separator;
        return this;
    }

    /**
     * Sets the universal color used for punctuation (separating children, aliases, declaring
     * optional or mandatory argument nodes, etc.).
     * <br>
     * If {@link #getLiteralSeparatorColor()} is not set, this color will be used instead.
     *
     * @param color the color (parsed through the <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle punctuationColor(final @NotNull String color) {
        this.punctuationColor = color;
        return this;
    }

    /*
     * LITERAL
     */

    /**
     * Gets the color of the separator between the aliases of a {@link it.fulminazzo.blocksmith.command.node.LiteralNode}.
     * <br>
     * If not set, {@link #getPunctuationColor()} will be used instead.
     *
     * @return the color
     */
    public @NotNull String getLiteralSeparatorColor() {
        return literalSeparatorColor == null ? getPunctuationColor() : literalSeparatorColor;
    }

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

    /**
     * Applies the given color to the text, following the
     * <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>.
     * Effectively, it adds enclosing color tags around the text.
     *
     * @param text the text
     * @param color the color
     * @return the formatted text
     */
    static @NotNull String colorize(final @NotNull String text, final @NotNull String color) {
        return String.format("<%1$s>%2$s</%1$s>", color, text);
    }

}
