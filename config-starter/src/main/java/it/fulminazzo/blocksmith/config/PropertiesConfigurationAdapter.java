package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Implementation of {@link BaseConfigurationAdapter} for Properties.
 */
final class PropertiesConfigurationAdapter implements BaseConfigurationAdapter {
    @Delegate
    private final @NotNull BaseConfigurationAdapter delegate;

    /**
     * Instantiates a new Properties configuration adapter.
     *
     * @param logger the logger to warn about errors
     */
    public PropertiesConfigurationAdapter(final @NotNull Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                new JavaPropsMapper(),
                logger,
                PropertiesCommentPropertyWriter.class
        );
    }

    /**
     * An implementation of {@link CommentPropertyWriter} for handling Properties comments.
     */
    static final class PropertiesCommentPropertyWriter extends CommentPropertyWriter {

        /**
         * Instantiates a new Properties comment property writer.
         *
         * @param base    the base
         * @param comment the comment
         */
        public PropertiesCommentPropertyWriter(final @NotNull BeanPropertyWriter base,
                                         final @NotNull Comment comment) {
            super(base, comment);
        }

        @Override
        protected void writeComment(final @NotNull JsonGenerator generator,
                                    final @NotNull Comment comment) throws IOException {
            for (String t : CommentUtils.getText(comment))
                generator.writeRaw(String.format("# %s\n", t));
        }

    }

}
