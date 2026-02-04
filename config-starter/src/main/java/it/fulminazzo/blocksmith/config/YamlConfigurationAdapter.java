package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Implementation of {@link ConfigurationAdapter} for YAML.
 */
public final class YamlConfigurationAdapter implements ConfigurationAdapter {
    @Delegate
    private final @NotNull JacksonConfigurationAdapter delegate;

    /**
     * Instantiates a new YAML configuration adapter.
     *
     * @param logger the logger to warn about errors
     */
    public YamlConfigurationAdapter(final @NotNull Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                new YAMLMapper(),
                logger,
                YamlCommentPropertyWriter.class
        );
    }

    /**
     * An implementation of {@link CommentPropertyWriter} for handling YAML comments.
     */
    static final class YamlCommentPropertyWriter extends CommentPropertyWriter {

        /**
         * Instantiates a new YAML comment property writer.
         *
         * @param base    the base
         * @param comment the comment
         */
        public YamlCommentPropertyWriter(final @NotNull BeanPropertyWriter base,
                                         final @NotNull Comment comment) {
            super(base, comment);
        }

        @Override
        protected void writeComment(final @NotNull JsonGenerator generator,
                                    final @NotNull Comment comment) throws IOException {
            String commentText = comment.value().replace("\\n", "\n");
            for (String t : commentText.split("\n"))
                generator.writeRaw("# " + t + "\n");
        }

    }

}
