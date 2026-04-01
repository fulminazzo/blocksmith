package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Implementation of {@link BaseConfigurationAdapter} for XML.
 */
final class XmlConfigurationAdapter implements BaseConfigurationAdapter {
    @Delegate
    private final @NotNull BaseConfigurationAdapter delegate;

    /**
     * Instantiates a new XML configuration adapter.
     *
     * @param logger the logger to warn about errors
     */
    public XmlConfigurationAdapter(final @NotNull Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                XmlMapper.builder()
                        .enable(ToXmlGenerator.Feature.WRITE_NULLS_AS_XSI_NIL)
                        .enable(SerializationFeature.INDENT_OUTPUT)
                        .propertyNamingStrategy(PascalCaseStrategy.INSTANCE)
                        .build(),
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
            PrettyPrinter prettyPrinter = generator.getPrettyPrinter();
            for (String t : CommentUtils.getText(comment)) {
                if (prettyPrinter instanceof DefaultXmlPrettyPrinter)
                    Reflect.on(prettyPrinter)
                            .get("_objectIndenter")
                            .invoke("writeIndentation",
                                    generator,
                                    Reflect.on(prettyPrinter).get("_nesting").get()
                            );
                generator.writeRaw(String.format("<!-- %s -->", t));
            }
        }

    }

    /**
     * Implements the Pascal case strategy for jackson.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class PascalCaseStrategy extends PropertyNamingStrategies.NamingBase {
        public static final @NotNull PascalCaseStrategy INSTANCE = new PascalCaseStrategy();

        @Override
        public @NotNull String translate(final @NotNull String propertyName) {
            StringBuilder result = new StringBuilder();
            String[] words = propertyName.split("[^a-zA-Z0-9]+");

            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(Character.toUpperCase(word.charAt(0)));
                    result.append(word.substring(1));
                }
            }

            return result.toString();
        }

    }

}
