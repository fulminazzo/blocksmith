package it.fulminazzo.blocksmith.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.serde.ObjectSerializer;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import it.fulminazzo.blocksmith.config.nightconfig.ConfigUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link BaseConfigurationAdapter} for TOML.
 */
final class TomlConfigurationAdapter implements BaseConfigurationAdapter {
    private final @NotNull BaseConfigurationAdapter delegate;

    /**
     * Instantiates a new TOML configuration adapter.
     *
     * @param logger the logger
     */
    public TomlConfigurationAdapter(final @NotNull Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                new TomlMapper()
                        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE),
                logger,
                null // will be handled by night-config
        );
    }

    @Override
    public @NotNull <T> T load(final @NotNull String data, final @NotNull Class<T> type) throws IOException {
        return delegate.load(data, type);
    }

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        return delegate.load(file, type);
    }

    @Override
    public @NotNull <T> T load(final @NotNull InputStream stream, final @NotNull Class<T> type) throws IOException {
        return delegate.load(stream, type);
    }

    @Override
    public @NotNull <T> String serialize(final @NotNull T configuration) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            store(output, configuration);
            return output.toString();
        }
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) throws IOException {
        Files.createDirectories(file.getParentFile().toPath());
        Config config = toNightConfig(configuration);
        newTomlWriter().write(config, file, WritingMode.REPLACE);
        indentArrays(file);
    }

    @Override
    public <T> void store(final @NotNull OutputStream stream, final @NotNull T configuration) throws IOException {
        Config config = toNightConfig(configuration);
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        newTomlWriter().write(config, writer);
    }

    private <T> @NotNull Config toNightConfig(@NotNull T configuration) {
        CommentedConfig config = (CommentedConfig) ObjectSerializer.standard().serialize(configuration, CommentedConfig::inMemory);
        removeNulls(config);
        ConfigUtils.fixPropertyNames(config);
        ConfigUtils.setComments(configuration, config);
        ConfigVersion.getVersion(configuration.getClass()).ifPresent(v -> config.set("version", v.getVersion()));
        return config;
    }

    private static void removeNulls(final @NotNull Config config) {
        for (Config.Entry entry : new ArrayList<>(config.entrySet())) {
            Object value = entry.getValue();
            if (value == null) config.remove(entry.getKey());
            if (value instanceof Config) removeNulls((Config) value);
        }
    }

    /**
     * Manually indents arrays in the given TOML file.
     *
     * @param file the file
     */
    static void indentArrays(final @NotNull File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (FileReader reader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(lines::add);
        }
        try (FileWriter writer = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            String indent = "";
            for (String line : lines) {
                if (!indent.isEmpty() && line.matches(" *] *")) indent = "";
                bufferedWriter.write(indent + line + "\n");
                if (line.matches("^ *[A-Za-z.0-9_-]+ *= *\\[ *$")) indent = "    ";
            }
        }
    }

    /**
     * Gets a new TOML writer with predefined configuration.
     *
     * @return the toml writer
     */
    static @NotNull TomlWriter newTomlWriter() {
        TomlWriter writer = new TomlWriter();
        writer.setIndent("");
        writer.setIndentArrayElementsPredicate(l -> !l.isEmpty());
        return writer;
    }

}
