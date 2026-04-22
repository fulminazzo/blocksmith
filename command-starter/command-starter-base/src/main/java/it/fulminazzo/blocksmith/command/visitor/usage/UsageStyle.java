package it.fulminazzo.blocksmith.command.visitor.usage;

import it.fulminazzo.blocksmith.command.argument.dto.Coordinate;
import it.fulminazzo.blocksmith.command.argument.dto.Position;
import it.fulminazzo.blocksmith.command.argument.dto.WorldPosition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the universal styling for the usage of {@link it.fulminazzo.blocksmith.command.node.CommandNode}s.
 * <br>
 * Each color follows the <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>.
 */
@Getter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UsageStyle {
    private static final @NotNull Map<Class<?>, String> DEFAULT_ARGUMENT_COLORS;

    private static final @NotNull String DEFAULT_NUMBER_COLOR = "light_purple";
    private static final @NotNull String DEFAULT_BOOLEAN_COLOR = "green";
    private static final @NotNull String DEFAULT_CHAR_SEQUENCE_COLOR = "white";
    private static final @NotNull String DEFAULT_LOCALE_COLOR = "blue";
    public static final @NotNull String DEFAULT_POSITION_COLOR = "dark_green";
    public static final @NotNull String DEFAULT_PLAYER_COLOR = "gold";
    public static final @NotNull String DEFAULT_SERVER_COLOR = "dark_aqua";

    private static final @NotNull String PUNCTUATION_COLOR_PLACEHOLDER = "<punctuation>";

    private static @NotNull UsageStyle instance = new UsageStyle();

    private final @NotNull Map<Class<?>, String> argumentColors = new HashMap<>();

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
     * MANDATORY ARGUMENT
     */

    private @NotNull String defaultArgumentColor = "yellow";
    private @NotNull String argumentFormat = colorize("<", PUNCTUATION_COLOR_PLACEHOLDER) +
            "%s" + colorize(">", PUNCTUATION_COLOR_PLACEHOLDER);

    /*
     * OPTIONAL ARGUMENT
     */

    private @NotNull String defaultOptionalArgumentColor = "aqua";
    private @NotNull String optionalArgumentFormat = colorize("[", PUNCTUATION_COLOR_PLACEHOLDER) +
            "%s" + colorize("]", PUNCTUATION_COLOR_PLACEHOLDER);

    /*
     * ARGUMENT
     */

    private @NotNull String greedyArgumentFormat = "%s...";

    static {
        DEFAULT_ARGUMENT_COLORS = new ConcurrentHashMap<>();
        registerDefaultArgumentColor(Byte.class, DEFAULT_NUMBER_COLOR);
        registerDefaultArgumentColor(Short.class, DEFAULT_NUMBER_COLOR);
        registerDefaultArgumentColor(Integer.class, DEFAULT_NUMBER_COLOR);
        registerDefaultArgumentColor(Long.class, DEFAULT_NUMBER_COLOR);
        registerDefaultArgumentColor(Float.class, DEFAULT_NUMBER_COLOR);
        registerDefaultArgumentColor(Double.class, DEFAULT_NUMBER_COLOR);
        registerDefaultArgumentColor(Boolean.class, DEFAULT_BOOLEAN_COLOR);
        registerDefaultArgumentColor(Character.class, DEFAULT_CHAR_SEQUENCE_COLOR);
        registerDefaultArgumentColor(String.class, DEFAULT_CHAR_SEQUENCE_COLOR);
        registerDefaultArgumentColor(Object.class, DEFAULT_CHAR_SEQUENCE_COLOR);
        registerDefaultArgumentColor(Locale.class, DEFAULT_LOCALE_COLOR);
        registerDefaultArgumentColor(Coordinate.class, DEFAULT_POSITION_COLOR);
        registerDefaultArgumentColor(Position.class, DEFAULT_POSITION_COLOR);
        registerDefaultArgumentColor(WorldPosition.class, DEFAULT_POSITION_COLOR);
    }

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
     * @param color the color (parsed through the
     *              <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
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
     * Gets the color of the separator between the aliases of a
     * {@link it.fulminazzo.blocksmith.command.node.LiteralNode}.
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
     * @param color the color (parsed through the
     *              <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
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
     * Sets the color of the separator between the aliases of a
     * {@link it.fulminazzo.blocksmith.command.node.LiteralNode}.
     *
     * @param color the color (parsed through the
     *              <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle literalSeparatorColor(final @NotNull String color) {
        this.literalSeparatorColor = color;
        return this;
    }

    /*
     * MANDATORY ARGUMENT
     */

    /**
     * Gets the color for displaying the name of a {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * <br>
     * First, it will search for a specific color from the given type.
     * If it fails, it defaults to {@link #getDefaultArgumentColor()}.
     *
     * @param type the type of the argument
     * @return the color
     */
    public @NotNull String getArgumentColor(final @NotNull Class<?> type) {
        return getArgumentColorOrDefault(type, getDefaultArgumentColor());
    }

    /**
     * Gets the format of a {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * The format uses the Java default format syntax (so {@code %s} will be replaced by the argument name).
     *
     * @return the format
     */
    public @NotNull String getArgumentFormat() {
        return argumentFormat.replace(PUNCTUATION_COLOR_PLACEHOLDER, getPunctuationColor());
    }

    /**
     * Sets the default color for displaying the name of a {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     *
     * @param color the color (parsed through the
     *              <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle defaultArgumentColor(final @NotNull String color) {
        this.defaultArgumentColor = color;
        return this;
    }

    /**
     * Sets the format of a {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * The format uses the Java default format syntax (so {@code %s} will be replaced by the argument name).
     * If the special placeholder {@link  #PUNCTUATION_COLOR_PLACEHOLDER} is used,
     * it will be replaced by the current {@link #getPunctuationColor()} color.
     *
     * @param format the format
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle argumentFormat(final @NotNull String format) {
        this.argumentFormat = format;
        return this;
    }

    /*
     * OPTIONAL ARGUMENT
     */

    /**
     * Gets the color for displaying the name of an optional
     * {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * <br>
     * First, it will search for a specific color from the given type.
     * If it fails, it defaults to {@link #getDefaultOptionalArgumentColor()}.
     *
     * @param type the type of the argument
     * @return the color
     */
    public @NotNull String getOptionalArgumentColor(final @NotNull Class<?> type) {
        return getArgumentColorOrDefault(type, getDefaultOptionalArgumentColor());
    }

    /**
     * Gets the format of an optional {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * The format uses the Java default format syntax (so {@code %s} will be replaced by the argument name).
     *
     * @return the format
     */
    public @NotNull String getOptionalArgumentFormat() {
        return optionalArgumentFormat.replace(PUNCTUATION_COLOR_PLACEHOLDER, getPunctuationColor());
    }

    /**
     * Sets the default color for displaying the name of an optional
     * {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     *
     * @param color the color (parsed through the
     *              <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle defaultOptionalArgumentColor(final @NotNull String color) {
        this.defaultOptionalArgumentColor = color;
        return this;
    }

    /**
     * Sets the format of an optional {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * The format uses the Java default format syntax (so {@code %s} will be replaced by the argument name).
     * If the special placeholder {@link  #PUNCTUATION_COLOR_PLACEHOLDER} is used,
     * it will be replaced by the current {@link #getPunctuationColor()} color.
     *
     * @param format the format
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle optionalArgumentFormat(final @NotNull String format) {
        this.optionalArgumentFormat = format;
        return this;
    }

    /*
     * ARGUMENT
     */

    /**
     * Sets the color for displaying the name of a {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * <br>
     * This color will <b>overwrite</b> any default styling of a node with the same type as the one specified.
     *
     * @param type  the type of the argument
     * @param color the color (parsed through the
     *              <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle setArgumentColor(final @NotNull Class<?> type, final @Nullable String color) {
        if (color != null) argumentColors.put(type, color);
        else argumentColors.remove(type);
        return this;
    }

    /**
     * Gets the format of a greedy {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * The format uses the Java default format syntax (so {@code %s} will be replaced by the argument name).
     *
     * @return the format
     */
    public @NotNull String getGreedyArgumentFormat() {
        return greedyArgumentFormat.replace(PUNCTUATION_COLOR_PLACEHOLDER, getPunctuationColor());
    }

    /**
     * Sets the format of a greedy {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}.
     * The format uses the Java default format syntax (so {@code %s} will be replaced by the argument name).
     * If the special placeholder {@link  #PUNCTUATION_COLOR_PLACEHOLDER} is used,
     * it will be replaced by the current {@link #getPunctuationColor()} color.
     *
     * @param format the format
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle greedyArgumentFormat(final @NotNull String format) {
        this.greedyArgumentFormat = format;
        return this;
    }


    private @NotNull String getArgumentColorOrDefault(final @NotNull Class<?> type, final @NotNull String defaultColor) {
        return argumentColors.getOrDefault(type, DEFAULT_ARGUMENT_COLORS.getOrDefault(type, defaultColor));
    }

    /**
     * Resets the styling to its default values.
     *
     * @return this object (for method chaining)
     */
    public @NotNull UsageStyle defaults() {
        instance = new UsageStyle();
        return instance;
    }

    /**
     * Registers a new default argument color for the given type.
     * <br>
     * <b>WARNING</b>: this change is <b>global</b>. It <b>cannot</b> be reverted.
     *
     * @param type  the type of the argument
     * @param color the color (parsed through the
     *              <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     */
    public static void registerDefaultArgumentColor(final @NotNull Class<?> type, final @NotNull String color) {
        DEFAULT_ARGUMENT_COLORS.put(type, color);
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
     * @param text  the text
     * @param color the color
     * @return the formatted text
     */
    static @NotNull String colorize(final @NotNull String text, final @NotNull String color) {
        return String.format("<%1$s>%2$s</%1$s>", color, text);
    }

}
