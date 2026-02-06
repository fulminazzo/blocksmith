package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Implementation of {@link ConfigurationAdapter} for XML.
 */
final class XmlConfigurationAdapter implements ConfigurationAdapter {
    @Delegate
    private final @NotNull ConfigurationAdapter delegate;

    /**
     * Instantiates a new XML configuration adapter.
     *
     * @param logger the logger to warn about errors
     */
    public XmlConfigurationAdapter(final @NotNull Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                new XmlMapper(),
                logger,
                XmlCommentPropertyWriter.class
        );
    }

    /**
     * An implementation of {@link CommentPropertyWriter} for handling XML comments.
     */
    static final class XmlCommentPropertyWriter extends CommentPropertyWriter {

        /**
         * Instantiates a new XML comment property writer.
         *
         * @param base    the base
         * @param comment the comment
         */
        public XmlCommentPropertyWriter(final @NotNull BeanPropertyWriter base,
                                        final @NotNull Comment comment) {
            super(base, comment);
        }

        @Override
        protected void writeComment(final @NotNull JsonGenerator generator,
                                    final @NotNull Comment comment) throws IOException {
            //TODO: implement
        }

    }

}
