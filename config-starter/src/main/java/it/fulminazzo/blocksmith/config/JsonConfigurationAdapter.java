package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Implementation of {@link ConfigurationAdapter} for YAML.
 */
final class JsonConfigurationAdapter implements ConfigurationAdapter {
    @Delegate
    private final @NotNull ConfigurationAdapter delegate;

    /**
     * Instantiates a new JSON configuration adapter.
     *
     * @param logger the logger
     */
    public JsonConfigurationAdapter(final @NotNull Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                new ObjectMapper(),
                logger,
                JsonCommentPropertyWriter.class
        );
    }

    /**
     * An implementation of {@link CommentPropertyWriter} for handling JSON comments (none).
     */
    static final class JsonCommentPropertyWriter extends CommentPropertyWriter {

        /**
         * Instantiates a new JSON comment property writer.
         *
         * @param base    the base
         * @param comment the comment
         */
        public JsonCommentPropertyWriter(final @NotNull BeanPropertyWriter base,
                                         final @NotNull Comment comment) {
            super(base, comment);
        }

        @Override
        protected void writeComment(final @NotNull JsonGenerator generator,
                                    final @NotNull Comment comment) {
        }

    }

}
