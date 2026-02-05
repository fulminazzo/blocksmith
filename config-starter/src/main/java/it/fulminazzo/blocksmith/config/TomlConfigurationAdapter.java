package it.fulminazzo.blocksmith.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import it.fulminazzo.blocksmith.config.nightconfig.ConfigUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

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
    public @NotNull <T> T load(@NotNull File file, @NotNull Class<T> type) throws IOException {
        return delegate.load(file, type);
    }

    @Override
    public <T> void store(@NotNull File file, @NotNull T configuration) throws IOException {
        delegate.store(file, configuration);
        try (CommentedFileConfig config = CommentedFileConfig.builder(file, TomlFormat.instance())
                .sync()
                .preserveInsertionOrder()
                .build()) {
            config.load();
            ConfigUtils.setComments(configuration, config);
            config.save();
        }
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
