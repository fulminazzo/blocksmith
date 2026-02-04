package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Implementation of {@link ConfigurationAdapter} for TOML.
 */
final class TomlConfigurationAdapter implements ConfigurationAdapter {
    @Delegate
    private final @NotNull ConfigurationAdapter delegate;

    /**
     * Instantiates a new TOML configuration adapter.
     *
     * @param logger the logger
     */
    public TomlConfigurationAdapter(final @NotNull Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                new TomlMapper(),
                logger,
                TomlCommentPropertyWriter.class
        );
    }

    /**
     * An implementation of {@link CommentPropertyWriter} for handling TOML comments.
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
                                    final @NotNull Comment comment) throws IOException {
            String commentText = comment.value().replace("\\n", "\n");
            for (String t : commentText.split("\n"))
                generator.writeRaw("# " + t + "\n");
            generator.writeNull();
        }

    }

}
