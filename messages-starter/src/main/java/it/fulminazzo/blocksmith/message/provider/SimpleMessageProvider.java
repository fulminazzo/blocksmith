package it.fulminazzo.blocksmith.message.provider;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link MessageProvider}.
 * Does NOT utilize {@link Locale} to determine the message.
 */
@RequiredArgsConstructor
final class SimpleMessageProvider implements MessageProvider {
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

    private final @NotNull Map<String, String> messages;

    @Override
    public @NotNull Component getMessage(final @NotNull String path,
                                         final @NotNull Locale locale) throws MessageNotFoundException {
        String message = messages.get(path);
        if (message == null) throw new MessageNotFoundException(path, locale);
        return mini.deserialize(legacyToMini(message));
    }

    private static @NotNull String legacyToMini(@NotNull String message) {
        Matcher matcher = LEGACY_CHAT_CODES_REGEX.matcher(message);
        while (matcher.find()) 
            message = message.replace(matcher.group(), CHAT_CODES.get(matcher.group(1).toLowerCase()));
        return message;
    }

}
