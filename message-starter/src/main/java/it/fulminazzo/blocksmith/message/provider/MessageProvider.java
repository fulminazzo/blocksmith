package it.fulminazzo.blocksmith.message.provider;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.util.MapUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
     * @param workingDir the working dir
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

}
