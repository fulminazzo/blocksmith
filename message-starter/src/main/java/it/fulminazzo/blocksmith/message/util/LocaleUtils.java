package it.fulminazzo.blocksmith.message.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A collection of utilities to work with {@link Locale}.
 */
public final class LocaleUtils {

    /**
     * Converts a string to a locale.
     *
     * @param locale the string
     * @return the locale
     */
    public static @NotNull Locale fromString(final @NotNull String locale) {
        return Locale.forLanguageTag(locale.replace("_", "-"));
    }

    /**
     * Converts a locale to a string.
     *
     * @param locale the locale
     * @return the string
     */
    public static @NotNull String toString(final @NotNull Locale locale) {
        return (locale.getLanguage() + "_" + locale.getCountry()).toLowerCase();
    }

}
