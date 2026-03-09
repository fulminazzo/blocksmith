package it.fulminazzo.blocksmith.message.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

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
    private static final @NotNull Map<String, String> CHAT_CODES = new HashMap<>() {{
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
    private static final @NotNull Pattern LEGACY_CHAT_CODES_REGEX = Pattern.compile("[&§]([" +
            CHAT_CODES.keySet().stream()
                    .map(c -> c + c.toUpperCase())
                    .collect(Collectors.joining()) +
            "])"
    );
    private static final @NotNull Pattern AMPERSAND_HEX_CODE_REGEX = Pattern.compile("&(#[0-9a-fA-F]{6})");
    private static final @NotNull Pattern SECTIONSIGN_HEX_CODE_REGEX = Pattern.compile("§x((?:§[0-9a-fA-F]){6})");

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

    private static @NotNull String legacyToMini(@NotNull String message) {
        Matcher matcher = AMPERSAND_HEX_CODE_REGEX.matcher(message);
        while (matcher.find())
            message = message.replace(matcher.group(), String.format("<%s>", matcher.group(1)));

        matcher = SECTIONSIGN_HEX_CODE_REGEX.matcher(message);
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
