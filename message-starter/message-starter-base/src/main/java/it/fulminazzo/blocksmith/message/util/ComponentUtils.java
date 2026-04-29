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
    private static final @NotNull MiniMessage MINI = MiniMessage.miniMessage();
    private static final @NotNull Map<String, String> CHAT_CODES;
    private static final @NotNull Pattern LEGACY_CHAT_CODES_REGEX;
    private static final @NotNull Pattern AMPERSAND_HEX_CODE_REGEX = Pattern.compile("&(#[0-9a-fA-F]{6})");
    private static final @NotNull Pattern SECTION_SIGN_HEX_CODE_REGEX = Pattern.compile("§x((?:§[0-9a-fA-F]){6})");

    public static final @NotNull String TRUNCATE_SUFFIX = "...";

    private static final char TAG_START = '<';
    private static final char TAG_END = '>';
    private static final char ESCAPE_CHAR = '\\';

    static {
        CHAT_CODES = new HashMap<>();
        CHAT_CODES.put("0", "<black>");
        CHAT_CODES.put("1", "<dark_blue>");
        CHAT_CODES.put("2", "<dark_green>");
        CHAT_CODES.put("3", "<dark_aqua>");
        CHAT_CODES.put("4", "<dark_red>");
        CHAT_CODES.put("5", "<dark_purple>");
        CHAT_CODES.put("6", "<gold>");
        CHAT_CODES.put("7", "<gray>");
        CHAT_CODES.put("8", "<dark_gray>");
        CHAT_CODES.put("9", "<blue>");
        CHAT_CODES.put("a", "<green>");
        CHAT_CODES.put("b", "<aqua>");
        CHAT_CODES.put("c", "<red>");
        CHAT_CODES.put("d", "<light_purple>");
        CHAT_CODES.put("e", "<yellow>");
        CHAT_CODES.put("f", "<white>");
        CHAT_CODES.put("k", "<obfuscated>");
        CHAT_CODES.put("l", "<bold>");
        CHAT_CODES.put("m", "<strikethrough>");
        CHAT_CODES.put("n", "<underlined>");
        CHAT_CODES.put("o", "<italic>");
        CHAT_CODES.put("r", "<reset>");

        LEGACY_CHAT_CODES_REGEX = Pattern.compile("[&§]([" +
                CHAT_CODES.keySet().stream()
                        .map(c -> c + c.toUpperCase())
                        .collect(Collectors.joining()) +
                "])"
        );
    }

    /**
     * Truncates the component text while maintaining any previous styling.
     * If the component {@link #actualLength(Component)} is less than the index,
     * the whole component is returned.
     *
     * @param component the component
     * @param length    the final length of the component
     * @return the component
     */
    public static @NotNull Component truncate(final @NotNull Component component,
                                              final int length) {
        final int actual = actualLength(component);
        if (actual <= length) return component;
        else if (length > 3) return subcomponent(component, 0, length - 3).append(Component.text(TRUNCATE_SUFFIX));
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
        int curr = 0;
        int tags = 0;
        boolean escaped = false;
        for (int i = 0; i < rawComponent.length; i++) {
            char c = rawComponent[i];
            if (!escaped && c == TAG_START) {
                builder.append(c);
                while (++i < rawComponent.length && ((c = rawComponent[i]) != TAG_END || tags > 0 || escaped)) {
                    if (c == TAG_START) tags++;
                    else if (c == TAG_END) tags--;
                    escaped = c == ESCAPE_CHAR;
                    builder.append(c);
                }
                if (i < rawComponent.length) builder.append(c);
            } else {
                if (curr >= to) break;
                else if (curr >= from) builder.append(c);
                escaped = c == ESCAPE_CHAR;
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
     * {@code Hello <rainbow>world</rainbow>, isn't
     * <blue><u><click:open_url:'https://docs.papermc.io/adventure/minimessage/'>
     * MiniMessage
     * </click></u></blue> fun?}
     * <br>
     * the length is <i>35</i> as the real text is
     * <br>
     * {@code Hello world, isn't MiniMessage fun?}
     *
     * @param component the component
     * @return the length
     */
    public static int actualLength(final @NotNull Component component) {
        final char[] rawComponent = toString(component).toCharArray();
        int length = 0;
        int tags = 0;
        boolean escaped = false;
        for (int i = 0; i < rawComponent.length; i++) {
            char c = rawComponent[i];
            if (!escaped && c == TAG_START) {
                while (++i < rawComponent.length && ((c = rawComponent[i]) != TAG_END || tags > 0 || escaped)) {
                    if (c == TAG_START) tags++;
                    else if (c == TAG_END) tags--;
                    escaped = c == ESCAPE_CHAR;
                }
            } else {
                escaped = c == ESCAPE_CHAR;
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
        return MINI.deserialize(legacyToMini(string));
    }

    /**
     * Converts the given component to a string.
     * Uses <a href="https://docs.papermc.io/adventure/minimessage/">MiniMessage</a>.
     *
     * @param component the component
     * @return the string
     */
    public static @NotNull String toString(final @NotNull Component component) {
        return MINI.serialize(component);
    }

    private static @NotNull String legacyToMini(@NotNull String message) {
        Matcher matcher = AMPERSAND_HEX_CODE_REGEX.matcher(message);
        while (matcher.find())
            message = message.replace(matcher.group(), String.format("<%s>", matcher.group(1)));

        matcher = SECTION_SIGN_HEX_CODE_REGEX.matcher(message);
        while (matcher.find())
            message = message.replace(
                    matcher.group(),
                    String.format("<%s>", "#" + matcher.group(1).replace("§", ""))
            );

        matcher = LEGACY_CHAT_CODES_REGEX.matcher(message);
        while (matcher.find())
            message = message.replace(matcher.group(), CHAT_CODES.get(matcher.group(1).toLowerCase()));

        return message;
    }

}
