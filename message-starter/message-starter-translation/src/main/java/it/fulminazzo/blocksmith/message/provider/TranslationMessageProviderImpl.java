package it.fulminazzo.blocksmith.message.provider;

import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of {@link TranslationMessageProvider}.
 */
final class TranslationMessageProviderImpl implements TranslationMessageProvider {
    private final @NotNull Map<Locale, MessageProvider> providers = new HashMap<>();
    private @NotNull Locale defaultLocale = LocaleUtils.fromString("en_us");

    @Override
    public @NotNull Component getMessage(final @NotNull String path,
                                         final @NotNull Locale locale) throws MessageNotFoundException {
        MessageProvider provider = providers.get(locale);
        if (provider == null) provider = providers.get(defaultLocale);
        if (provider == null)
            throw new IllegalArgumentException("No provider registered for locale: " + LocaleUtils.toString(locale));
        return provider.getMessage(path, locale);
    }

    @Override
    public @NotNull TranslationMessageProvider setDefaultLocale(final @NotNull Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        return this;
    }

    @Override
    public void registerProvider(final @NotNull Locale locale,
                                 final @NotNull MessageProvider provider) {
        providers.put(locale, provider);
    }

}
