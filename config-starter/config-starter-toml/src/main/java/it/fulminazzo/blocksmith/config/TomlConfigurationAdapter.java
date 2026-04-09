package it.fulminazzo.blocksmith.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.serde.ObjectSerializer;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import it.fulminazzo.blocksmith.config.nightconfig.NightConfigUtils;
import it.fulminazzo.blocksmith.naming.CaseConverter;
import it.fulminazzo.blocksmith.naming.Convention;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BaseConfigurationAdapter} for TOML.
 */
final class TomlConfigurationAdapter implements BaseConfigurationAdapter {
    private static final @NotNull Convention tomlNamingConvention = Convention.SNAKE_CASE;

    private final @NotNull BaseConfigurationAdapter delegate;
    private final @NotNull TomlParser parser;
    private final @NotNull TomlWriter writer;

    /**
     * Instantiates a new TOML configuration adapter.
     *
     * @param logger the logger
     */
    public TomlConfigurationAdapter(final @NotNull Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                new TomlMapper()
                        .setPropertyNamingStrategy(Reflect.on(PropertyNamingStrategies.class)
                                .get(tomlNamingConvention.name())
                                .get()),
                logger,
                null // will be handled by night-config
        );
        this.parser = TomlFormat.instance().createParser();
        this.writer = new TomlWriter();
        writer.setIndent("");
        writer.setIndentArrayElementsPredicate(l -> !l.isEmpty());
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> loadComments(final @NotNull InputStream stream) {
        CommentedConfig config = parser.parse(stream);
        return toCommentedMap(config.getComments());
    }

    @Override
    public <T> @NotNull T load(final @NotNull String data, final @NotNull Class<T> type) throws IOException {
        return ConfigUtils.checkMap(delegate.load(data, type), tomlNamingConvention, ConfigUtils.javaNamingConvention);
    }

    @Override
    public <T> @NotNull T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        return ConfigUtils.checkMap(delegate.load(file, type), tomlNamingConvention, ConfigUtils.javaNamingConvention);
    }

    @Override
    public <T> @NotNull T load(final @NotNull InputStream stream, final @NotNull Class<T> type) throws IOException {
        return ConfigUtils.checkMap(delegate.load(stream, type), tomlNamingConvention, ConfigUtils.javaNamingConvention);
    }

    @Override
    public <T> @NotNull String serialize(final @NotNull T configuration) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            store(output, configuration);
            return output.toString();
        }
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) throws IOException {
        Files.createDirectories(file.getParentFile().toPath());
        Config config = toNightConfig(configuration);
        writer.write(config, file, WritingMode.REPLACE);
        indentArrays(file);
    }

    @Override
    public <T> void store(final @NotNull OutputStream stream, final @NotNull T configuration) throws IOException {
        try (OutputStreamWriter objectWriter = new OutputStreamWriter(stream)) {
            Config config = toNightConfig(configuration);
            writer.write(config, objectWriter);
        }
    }

    private static @NotNull Map<String, List<String>> toCommentedMap(final @NotNull Map<String, UnmodifiableCommentedConfig.CommentNode> nodes) {
        final Map<String, List<String>> keysComments = new HashMap<>();
        for (Map.Entry<String, UnmodifiableCommentedConfig.CommentNode> entry : nodes.entrySet()) {
            String key = CaseConverter.convert(entry.getKey(), tomlNamingConvention, it.fulminazzo.blocksmith.config.ConfigUtils.javaNamingConvention);
            UnmodifiableCommentedConfig.CommentNode value = entry.getValue();
            String comment = value.getComment();
            if (comment != null) keysComments.put(key, Arrays.stream(comment.split("\n"))
                    .map(String::trim)
                    .collect(Collectors.toUnmodifiableList()));
            if (value.getChildren() != null)
                toCommentedMap(value.getChildren()).forEach((k, c) ->
                        keysComments.put(key + "." + k, c)
                );
        }
        return keysComments;
    }

    private <T> @NotNull Config toNightConfig(@NotNull T configuration) {
        configuration = ConfigUtils.checkMap(configuration, ConfigUtils.javaNamingConvention, tomlNamingConvention);
        CommentedConfig config = (CommentedConfig) ObjectSerializer.standard().serialize(configuration, CommentedConfig::inMemory);
        removeNulls(config);
        NightConfigUtils.fixPropertyNames(config);
        NightConfigUtils.setComments(configuration, config);
        ConfigVersion.getVersion(configuration.getClass()).ifPresent(v -> config.set(ConfigVersion.PROPERTY_NAME, v.getVersion()));
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

}
