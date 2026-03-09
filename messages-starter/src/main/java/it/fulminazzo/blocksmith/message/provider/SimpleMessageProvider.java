package it.fulminazzo.blocksmith.message.provider;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * Basic implementation of {@link MessageProvider}.
 * Does NOT utilize {@link Locale} to determine the message.
 */
@RequiredArgsConstructor
final class SimpleMessageProvider implements MessageProvider {
    private final @NotNull Map<String, String> messages;

    @Override
    public @NotNull Component getMessage(final @NotNull String path,
                                         final @NotNull Locale locale) throws MessageNotFoundException {
        String message = messages.get(path);
        if (message == null) throw new MessageNotFoundException(path, locale);
        else return Component.text(message);
    }

}
