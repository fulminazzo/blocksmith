package it.fulminazzo.blocksmith.message.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A collection of utilities to work with <a href="https://docs.papermc.io/adventure/">text adventure</a> components.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComponentUtils {
    private static final @NotNull MiniMessage mini = MiniMessage.miniMessage();
    private static final @NotNull Map<String, String> chatCodes = new HashMap<>() {{
        put("0", "<black>");
        put("1", "<dark_blue>");
        put("2", "<dark_green>");
        put("3", "<dark_aqua>");
        put("4", "<dark_red>");
        put("5", "<dark_purple>");
        put("6", "<gold>");
        put("7", "<gray>");
        put("8", "<dark_gray>");
        put("9", "<blue>");
        put("a", "<green>");
        put("b", "<aqua>");
        put("c", "<red>");
        put("d", "<light_purple>");
        put("e", "<yellow>");
        put("f", "<white>");
        put("k", "<obfuscated>");
        put("l", "<bold>");
        put("m", "<strikethrough>");
        put("n", "<underlined>");
        put("o", "<italic>");
        put("r", "<reset>");
    }};
    private static final @NotNull Pattern legacyChatCodesRegex = Pattern.compile("[&§]([" +
            chatCodes.keySet().stream()
                    .map(c -> c + c.toUpperCase())
                    .collect(Collectors.joining()) +
            "])"
    );
    private static final @NotNull Pattern ampersandHexCodeRegex = Pattern.compile("&(#[0-9a-fA-F]{6})");
    private static final @NotNull Pattern sectionSignHexCodeRegex = Pattern.compile("§x((?:§[0-9a-fA-F]){6})");

    private static final char tagStart = '<', tagEnd = '>', escapeChar = '\\';

    /**
     * Truncates the component text while maintaining any previous styling.
     * If the component {@link #actualLength(Component)} is less than the index,
     * the whole component is returned.
     *
     * @param component the component
     * @param length the final length of the component
     * @return the component
     */
    public static @NotNull Component truncate(final @NotNull Component component,
                                              final int length) {
        final int actual = actualLength(component);
        if (actual <= length) return component;
        else if (length > 3) return subcomponent(component, 0, length - 3).append(Component.text("..."));
        else return subcomponent(component, 0, length);
    }

    /**
     * Cuts the component text at the given indexes, while maintaining any previous styling.
     *
     * @param component the component
     * @param from      the index to start cutting from
     * @param to        the index to end cutting to
     * @return the newly cut component
     */
    public static @NotNull Component subcomponent(final @NotNull Component component,
                                                  final @Range(from = 0, to = Integer.MAX_VALUE) int from,
                                                  final @Range(from = 0, to = Integer.MAX_VALUE) int to) {
        int length = actualLength(component);
        if (to > length || from > to || from < 0)
            throw new IndexOutOfBoundsException(String.format("Range [%s, %s) out of bounds for length %s", from, to, length));

        final StringBuilder builder = new StringBuilder();
        final char[] rawComponent = toString(component).toCharArray();
        int curr = 0, tags = 0;
        boolean escaped = false;
        for (int i = 0; i < rawComponent.length; i++) {
            char c = rawComponent[i];
            if (!escaped && c == tagStart) {
                builder.append(c);
                while (++i < rawComponent.length && ((c = rawComponent[i]) != tagEnd || tags > 0 || escaped)) {
                    if (c == tagStart) tags++;
                    else if (c == tagEnd) tags--;
                    escaped = c == escapeChar;
                    builder.append(c);
                }
                if (i < rawComponent.length) builder.append(c);
            } else {
                if (curr >= to) break;
                else if (curr >= from) builder.append(c);
                escaped = c == escapeChar;
                curr++;
            }
        }

        return toComponent(builder.toString());
    }

    /**
     * Returns the actual length of the text contained in the component.
     * The text of the component refers to the characters that identify
     * the raw text, excluding any formatting, styling or action tags.
     * <br>
     * For example, in
     * <br>
     * <code>Hello &lt;rainbow&gt;world&lt;/rainbow&gt;, isn't
     * &lt;blue&gt;&lt;u&gt;&lt;click:open_url:'https://docs.papermc.io/adventure/minimessage/'&gt;
     * MiniMessage
     * &lt;/click&gt;&lt;/u&gt;&lt;/blue&gt; fun?</code>
     * <br>
     * the length is <i>35</i> as the real text is
     * <br>
     * <code>Hello world, isn't MiniMessage fun?</code>
     *
     * @param component the component
     * @return the length
     */
    public static int actualLength(final @NotNull Component component) {
        final char[] rawComponent = toString(component).toCharArray();
        int length = 0, tags = 0;
        boolean escaped = false;
        for (int i = 0; i < rawComponent.length; i++) {
            char c = rawComponent[i];
            if (!escaped && c == tagStart) {
                while (++i < rawComponent.length && ((c = rawComponent[i]) != tagEnd || tags > 0 || escaped)) {
                    if (c == tagStart) tags++;
                    else if (c == tagEnd) tags--;
                    escaped = c == escapeChar;
                }
            } else {
                escaped = c == escapeChar;
                length++;
            }
        }
        return length;
    }

    /**
     * Converts the given string to a component.
     * Supports both <a href="https://docs.papermc.io/adventure/minimessage/">MiniMessage</a> and
     * legacy formats with ampersand and section sign.
     *
     * @param string the string
     * @return the component
     */
    public static @NotNull Component toComponent(final @NotNull String string) {
        return mini.deserialize(legacyToMini(string));
    }

    /**
     * Converts the given component to a string.
     * Uses <a href="https://docs.papermc.io/adventure/minimessage/">MiniMessage</a>.
     *
     * @param component the component
     * @return the string
     */
    public static @NotNull String toString(final @NotNull Component component) {
        return mini.serialize(component);
    }

    private static @NotNull String legacyToMini(@NotNull String message) {
        Matcher matcher = ampersandHexCodeRegex.matcher(message);
        while (matcher.find())
            message = message.replace(matcher.group(), String.format("<%s>", matcher.group(1)));

        matcher = sectionSignHexCodeRegex.matcher(message);
        while (matcher.find())
            message = message.replace(
                    matcher.group(),
                    String.format("<%s>", "#" + matcher.group(1).replace("§", ""))
            );

        matcher = legacyChatCodesRegex.matcher(message);
        while (matcher.find())
            message = message.replace(matcher.group(), chatCodes.get(matcher.group(1).toLowerCase()));

        return message;
    }

}
