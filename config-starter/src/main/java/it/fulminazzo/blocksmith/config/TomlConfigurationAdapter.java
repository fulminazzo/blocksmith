package it.fulminazzo.blocksmith.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.serde.ObjectSerializer;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import it.fulminazzo.blocksmith.config.nightconfig.ConfigUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ConfigurationAdapter} for TOML.
 */
final class TomlConfigurationAdapter implements ConfigurationAdapter {
    private final @NotNull ConfigurationAdapter delegate;

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
                TomlCommentPropertyWriter.class
        );
    }

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        return delegate.load(file, type);
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) throws IOException {
        CommentedConfig config = (CommentedConfig) ObjectSerializer.standard()
                .serialize(configuration, CommentedConfig::inMemory);
        ConfigUtils.fixPropertyNames(config);
        ConfigUtils.setComments(configuration, config);
        newTomlWriter().write(config, file, WritingMode.REPLACE);
        indentArrays(file);
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

    /**
     * An implementation of {@link CommentPropertyWriter} for handling TOML comments.
     * (These are actually handled by night-config)
     */
    static final class TomlCommentPropertyWriter extends CommentPropertyWriter {

        /**
         * Instantiates a new TOML comment property writer.
         *
         * @param base    the base
         * @param comment the comment
         */
        public TomlCommentPropertyWriter(final @NotNull BeanPropertyWriter base,
                                         final @NotNull Comment comment) {
            super(base, comment);
        }

        @Override
        protected void writeComment(final @NotNull JsonGenerator generator,
                                    final @NotNull Comment comment) {
        }

    }

}
