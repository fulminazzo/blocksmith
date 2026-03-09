package it.fulminazzo.blocksmith.message.provider;

import it.fulminazzo.blocksmith.ProjectInfo;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;
import org.joor.ReflectException;

import java.util.Locale;

/**
 * A special {@link MessageProvider} that uses the given {@link Locale} to find
 * the most appropriate message from the loaded internal providers.
 */
public interface TranslationMessageProvider extends MessageProvider {

    /**
     * Sets the default locale.
     *
     * @param defaultLocale the default locale to use in case the requested one fails
     * @return this provider
     */
    @NotNull TranslationMessageProvider setDefaultLocale(final @NotNull Locale defaultLocale);

    /**
     * Registers a new provider.
     *
     * @param locale   the locale
     * @param provider the provider
     */
    void registerProvider(final @NotNull Locale locale, final @NotNull MessageProvider provider);

    /**
     * Creates a new Translation message provider.
     *
     * @return the translation message provider
     */
    static @NotNull TranslationMessageProvider newProvider() {
        try {
            return Reflect.onClass(TranslationMessageProvider.class.getCanonicalName() + "Impl").create().get();
        } catch (ReflectException e) {
            String moduleName = String.format("%s.%s:%s-translation",
                    ProjectInfo.GROUP,
                    ProjectInfo.PROJECT_NAME,
                    ProjectInfo.MODULE_NAME
            );
            throw new IllegalStateException(
                    String.format("Could not find valid implementation of %s. ", TranslationMessageProvider.class.getSimpleName()) +
                            String.format("Please check that the module %s is correctly installed.", moduleName)
            );
        }
    }

}
