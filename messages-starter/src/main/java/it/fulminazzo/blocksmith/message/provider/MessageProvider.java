package it.fulminazzo.blocksmith.message.provider;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Provides messages based on their code (path in the associated configuration).
 */
public interface MessageProvider {

    /**
     * Provides a message based on the given path and locale.
     *
     * @param path   the path
     * @param locale the locale
     * @return the message
     */
    @NotNull Component getMessage(final @NotNull String path,
                                  final @NotNull Locale locale);

}
