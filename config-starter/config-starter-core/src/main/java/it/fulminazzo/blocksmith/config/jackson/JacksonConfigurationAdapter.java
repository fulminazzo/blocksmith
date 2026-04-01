package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.Optional;

/**
 * A special implementation of {@link BaseConfigurationAdapter}
 * that uses the <a href="https://github.com/FasterXML/jackson">jackson project</a>
 * for serialization and deserialization.
 */
public final class JacksonConfigurationAdapter implements BaseConfigurationAdapter {
    private static final @NotNull String versionPropertyName = "version";
    private static final double baseVersion = 1.0;
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
        Map<String, Object> data = mapper.readValue(file, new TypeReference<>() {
        });
        @NotNull Optional<ConfigVersion> versionOpt = ConfigVersion.getVersion(type);
        if (versionOpt.isPresent()) {
            final ConfigVersion version = versionOpt.get();
            data = MapUtils.flatten(data);

            double latest = version.getVersion();
            Object rawVersion = data.remove(versionPropertyName);
            Double currentVersion;
            try {
                currentVersion = (Double) rawVersion;
                if (currentVersion == null) throw new ClassCastException();
            } catch (ClassCastException e) {
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
                T value = mapper.convertValue(data, type);
                store(file, value);
                return value;
            }

            data = MapUtils.unflatten(data);
        }
        return mapper.convertValue(data, type);
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) throws IOException {
        Files.createDirectories(file.getParentFile().toPath());
        ObjectMapper writer = ConfigVersion.getVersion(configuration.getClass()).isPresent()
                ? mapper.copy().addMixIn(configuration.getClass(), VersionMixin.class)
                : mapper;
        writer.writeValue(file, configuration);
    }

}
