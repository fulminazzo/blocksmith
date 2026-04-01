package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import it.fulminazzo.blocksmith.config.BaseConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigVersion;
import it.fulminazzo.blocksmith.util.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * A special implementation of {@link BaseConfigurationAdapter}
 * that uses the <a href="https://github.com/FasterXML/jackson">jackson project</a>
 * for serialization and deserialization.
 */
@SuppressWarnings("unchecked")
public final class JacksonConfigurationAdapter implements BaseConfigurationAdapter {
    private static final @NotNull SimpleDateFormat backupTimeFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");

    private final @NotNull ObjectMapper mapper;
    private final @NotNull Logger logger;

    /**
     * Instantiates a new Jackson configuration adapter.
     *
     * @param mapper                    the object mapper
     * @param logger                    the logger
     * @param commentPropertyWriterType the type of {@link CommentPropertyWriter} responsible for writing comments
     */
    public JacksonConfigurationAdapter(final @NotNull ObjectMapper mapper,
                                       final @NotNull Logger logger,
                                       final @Nullable Class<? extends CommentPropertyWriter> commentPropertyWriterType) {
        this.mapper = JacksonUtils.setupMapper(mapper, logger, commentPropertyWriterType);
        this.logger = logger;
    }

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        JsonNode tree = mapper.readTree(file);
        if (tree.isObject()) {
            Map<String, Object> data = mapper.convertValue(tree, new TypeReference<>() {
            });
            @NotNull Optional<ConfigVersion> versionOpt = ConfigVersion.getVersion(type);
            if (versionOpt.isPresent()) {
                final ConfigVersion version = versionOpt.get();
                unapplyNamingStrategy(data, mapper.getPropertyNamingStrategy());
                data = MapUtils.flatten(data);

                Object rawVersion = data.get("version");
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
                    logger.info("Migrating configuration '{}' from version {} to version {}", file.getName(), currentVersion, latest);

                    String tmp = file.getName();
                    String name = tmp.substring(0, tmp.lastIndexOf('.'));
                    String extension = tmp.substring(tmp.lastIndexOf('.') + 1);
                    File backupFile = new File(file.getParentFile(), String.format("%s-%s.%s.bk",
                            name,
                            backupTimeFormat.format(System.currentTimeMillis()),
                            extension
                    ));
                    Files.move(file.toPath(), backupFile.toPath());
                    logger.info("Configuration '{}' has been backed up to '{}'", file.getName(), backupFile.getName());

                    data = version.applyMigrations(currentVersion, data);
                    data = MapUtils.unflatten(data);
                    data.put("version", latest);
                    applyNamingStrategy(data, mapper.getPropertyNamingStrategy());
                    store(file, data);
                    return load(file, type);
                }
            }
        }
        return mapper.readValue(file, type);
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) throws IOException {
        Files.createDirectories(file.getParentFile().toPath());
        ObjectMapper writer = ConfigVersion.getVersion(configuration.getClass()).isPresent()
                ? mapper.copy().addMixIn(configuration.getClass(), VersionMixin.class)
                : mapper;
        writer.writeValue(file, configuration);
    }

    private void applyNamingStrategy(final @NotNull Map<String, Object> data,
                                     final @Nullable PropertyNamingStrategy strategy) {
        if (strategy == null) return;
        for (String key : new ArrayList<>(data.keySet())) {
            Object value = data.remove(key);
            if (value instanceof Map) applyNamingStrategy((Map<String, Object>) value, strategy);
            data.put(strategy.nameForField(null, null, key), value);
        }
    }

    private void unapplyNamingStrategy(final @NotNull Map<String, Object> data,
                                       final @Nullable PropertyNamingStrategy strategy) {
        if (strategy == null) return;
        for (String key : new ArrayList<>(data.keySet())) {
            Object value = data.remove(key);
            if (value instanceof Map) unapplyNamingStrategy((Map<String, Object>) value, strategy);
            if (strategy.equals(PropertyNamingStrategies.KEBAB_CASE)) key = dashedCaseToCamel(key, '-');
            else if (strategy.equals(PropertyNamingStrategies.SNAKE_CASE)) key = dashedCaseToCamel(key, '_');
            else key = key.substring(0, 1).toLowerCase() + key.substring(1);
            data.put(key, value);
        }
    }

    private static String dashedCaseToCamel(final @NotNull String string,
                                            final char dash) {
        if (!string.contains(String.valueOf(dash))) return string;

        StringBuilder sb = new StringBuilder();
        boolean makeUpper = false;

        for (char c : string.toCharArray()) {
            if (c == dash) {
                makeUpper = true;
            } else if (makeUpper) {
                sb.append(Character.toUpperCase(c));
                makeUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
