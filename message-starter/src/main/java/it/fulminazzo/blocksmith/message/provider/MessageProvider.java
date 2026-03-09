package it.fulminazzo.blocksmith.message.provider;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import it.fulminazzo.blocksmith.util.MapUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
     * @throws MessageNotFoundException in case the message was not found
     */
    @NotNull Component getMessage(final @NotNull String path,
                                  final @NotNull Locale locale) throws MessageNotFoundException;

    /**
     * Creates a new MessageProvider from the given map.
     *
     * @param messages the messages
     * @return the message provider
     */
    static @NotNull MessageProvider memory(@NotNull Map<String, Object> messages) {
        messages = MapUtils.flattenMap(messages);
        Map<String, String> stringMessages = new HashMap<>();
        messages.forEach((key, value) -> {
            if (value != null) stringMessages.put(key, value.toString());
        });
        return new SimpleMessageProvider(stringMessages);
    }

    /**
     * Creates a new MessageProvider from the given resource.
     * The lookup process is as follows:
     * <ol>
     *     <li>check if the file exists on disk. If it does not, it will be loaded from the bundled resource;</li>
     *     <li>loads the file from disk as a generic Map;</li>
     *     <li>creates the new MessageProvider from the found map.</li>
     * </ol>
     *
     * @param workingDir the working directory
     * @param resource   the resource
     * @return the message provider
     * @throws IOException in case of any errors
     */
    @SuppressWarnings("unchecked")
    static @NotNull MessageProvider resource(final @NotNull File workingDir,
                                             final @NotNull String resource) throws IOException {
        File file = new File(workingDir, resource);
        if (!file.exists()) {
            Files.createDirectories(file.toPath().getParent());
            Files.createFile(file.toPath());
            try (InputStream input = MessageProvider.class.getResourceAsStream("/" + resource);
                 OutputStream output = new FileOutputStream(file)) {
                if (input == null)
                    throw new IllegalArgumentException("Could not find resource: " + resource);
                input.transferTo(output);
            }
        }
        ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(
                null,
                ConfigurationFormat.fromFile(file)
        );
        Map<String, Object> messages = adapter.load(file, Map.class);
        return memory(messages);
    }

    /**
     * Creates a new MessageProvider that supports translations.
     * The provider expects a resource directory where all the default translations are stored.
     * Will then load them from disk, based on the locale specified in their name.
     *
     * @param workingDir   the working directory
     * @param resourcesDir the resources directory
     * @param format       the format of the translation files
     * @return the translation message provider
     * @throws IOException in case of any errors
     */
    static @NotNull TranslationMessageProvider translation(final @NotNull File workingDir,
                                                           final @NotNull String resourcesDir,
                                                           final @NotNull ConfigurationFormat format) throws IOException {
        File directory = new File(workingDir, resourcesDir);
        if (!directory.isDirectory()); //TODO: resources lookup
        TranslationMessageProvider provider = TranslationMessageProvider.newProvider();
        for (File file : Objects.requireNonNull(directory.listFiles(), "Could not read files of directory: " + directory.getAbsolutePath())) {
            if (format.isValidFile(file)) {
                String fileName = file.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                Locale locale = LocaleUtils.fromString(fileName);
                provider.registerProvider(
                        locale,
                        resource(directory, file.getName())
                );
            }
        }
        return provider;
    }

}
