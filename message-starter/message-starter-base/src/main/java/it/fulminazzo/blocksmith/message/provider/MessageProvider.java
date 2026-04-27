package it.fulminazzo.blocksmith.message.provider;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import it.fulminazzo.blocksmith.naming.Convention;
import it.fulminazzo.blocksmith.util.MapUtils;
import it.fulminazzo.blocksmith.util.ResourceUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    static @NotNull MessageProvider memory(final @NotNull Map<String, Object> messages) {
        return new SimpleMessageProvider(MapUtils.stringify(messages));
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
     * @param logger     the logger to display any loading-storing errors
     * @return the message provider
     * @throws IOException in case of any errors
     */
    static @NotNull MessageProvider resource(final @NotNull File workingDir,
                                             final @NotNull String resource,
                                             final @NotNull Logger logger) throws IOException {
        return resource(workingDir, resource, logger, null);
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
     * @param workingDir     the working directory
     * @param resource       the resource
     * @param logger         the logger to display any loading-storing errors
     * @param version the current version of the messages and migrations handler
     * @return the message provider
     * @throws IOException in case of any errors
     */
    @SuppressWarnings("unchecked")
    static @NotNull MessageProvider resource(final @NotNull File workingDir,
                                             final @NotNull String resource,
                                             final @NotNull Logger logger,
                                             final @Nullable MessageVersion version) throws IOException {
        File file = ResourceUtils.extractIfAbsent(resource, workingDir);
        ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(
                logger,
                ConfigurationFormat.fromExtension(file.getName())
        );
        Map<String, Object> messages = adapter.load(file, Map.class);
        messages = MapUtils.convertNames(messages, Convention.CAMEL_CASE, Convention.KEBAB_CASE);
        if (version != null) {
            Object rawVersion = messages.remove(MessageVersion.PROPERTY_NAME);
            double latest = version.getVersion();
            Double currentVersion = null;
            if (rawVersion != null)
                try {
                    currentVersion = Double.parseDouble(rawVersion.toString());
                } catch (NumberFormatException ignored) {
                }
            if (currentVersion == null) {
                logger.warn("Invalid version '{}'. Expected a decimal number.", rawVersion);
                logger.warn("Using latest version {}", latest);
                currentVersion = latest;
            }

            if (currentVersion != latest) {
                logger.info("Migrating messages file '{}' from version {} to version {}", file.getName(), currentVersion, latest);

                String tmp = file.getName();
                String name = tmp.substring(0, tmp.lastIndexOf('.'));
                String extension = tmp.substring(tmp.lastIndexOf('.') + 1);
                File backupFile = new File(file.getParentFile(), String.format("%s-%s.%s.bk",
                        name,
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS").format(System.currentTimeMillis()),
                        extension
                ));
                Files.move(file.toPath(), backupFile.toPath());
                logger.info("Messages file '{}' has been backed up to '{}'", file.getName(), backupFile.getName());

                Map<String, Object> resourceMessages = adapter.loadFromResource(resource, Map.class);
                resourceMessages = MapUtils.convertNames(resourceMessages, Convention.CAMEL_CASE, Convention.KEBAB_CASE);
                messages = version.applyMigrations(currentVersion, messages, resourceMessages);
                messages.put(MessageVersion.PROPERTY_NAME, latest);
                messages = MapUtils.convertNames(messages, Convention.KEBAB_CASE, Convention.CAMEL_CASE);
                adapter.store(file, messages);
                return resource(workingDir, resource, logger, version);
            }
        }
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
     * @param logger       the logger to display any loading-storing errors
     * @return the translation message provider
     * @throws IOException in case of any errors
     */
    static @NotNull TranslationMessageProvider translation(final @NotNull File workingDir,
                                                           final @NotNull String resourcesDir,
                                                           final @NotNull ConfigurationFormat format,
                                                           final @NotNull Logger logger) throws IOException {
        return translation(workingDir, resourcesDir, format, logger, null);
    }

    /**
     * Creates a new MessageProvider that supports translations.
     * The provider expects a resource directory where all the default translations are stored.
     * Will then load them from disk, based on the locale specified in their name.
     *
     * @param workingDir     the working directory
     * @param resourcesDir   the resources directory
     * @param format         the format of the translation files
     * @param logger         the logger to display any loading-storing errors
     * @param version the current version of the messages and migrations handler
     * @return the translation message provider
     * @throws IOException in case of any errors
     */
    static @NotNull TranslationMessageProvider translation(final @NotNull File workingDir,
                                                           final @NotNull String resourcesDir,
                                                           final @NotNull ConfigurationFormat format,
                                                           final @NotNull Logger logger,
                                                           final @Nullable MessageVersion version) throws IOException {
        File directory = new File(workingDir, resourcesDir);
        TranslationMessageProvider provider = TranslationMessageProvider.newProvider();
        final Collection<String> resources;
        if (!directory.isDirectory()) {
            Files.createDirectories(directory.toPath());
            resources = ResourceUtils.listResources(resourcesDir);
        } else {
            File[] files = Objects.requireNonNull(directory.listFiles(), "Could not read files of directory: " + directory.getAbsolutePath());
            resources = Arrays.stream(files).map(File::getName).collect(Collectors.toList());
        }
        for (String resourceName : resources) {
            if (format.matches(resourceName)) {
                String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1, resourceName.lastIndexOf('.'));
                Locale locale = LocaleUtils.fromString(fileName);
                if (locale.getLanguage().isEmpty() || locale.getCountry().isEmpty()) {
                    logger.warn("Ignoring invalid translation file {}. The expected format is %language%_%country%.{}", resourceName, format.name().toLowerCase());
                    continue;
                }
                provider.registerProvider(locale, resource(directory, resourceName, logger, version));
            }
        }
        return provider;
    }

}
