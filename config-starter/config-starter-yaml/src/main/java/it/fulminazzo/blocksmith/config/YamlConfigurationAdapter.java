package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.events.CommentEvent;

import java.io.IOException;
import java.io.Writer;

/**
 * Implementation of {@link BaseConfigurationAdapter} for YAML.
 */
final class YamlConfigurationAdapter implements BaseConfigurationAdapter {
    @Delegate
    private final @NotNull BaseConfigurationAdapter delegate;

    /**
     * Instantiates a new YAML configuration adapter.
     *
     * @param logger the logger to warn about errors
     */
    public YamlConfigurationAdapter(final @NotNull Logger logger) {
        DumperOptions options = new DumperOptions();
        options.setProcessComments(true);
        this.delegate = new JacksonConfigurationAdapter(
                new YAMLMapper(new SingleQuoteYAMLFactory(
                        YAMLFactory.builder().dumperOptions(options)
                ))
                        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                        .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
                        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE),
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
                                    final @NotNull Comment comment) {
            for (String t : CommentUtils.getText(comment))
                Reflect.on(generator).invoke("_emit",
                        new CommentEvent(CommentType.BLOCK, " " + t, null, null)
                );
        }

    }

    /**
     * A special {@link YAMLFactory} that uses {@link SingleQuoteYAMLGenerator} as generator.
     */
    static class SingleQuoteYAMLFactory extends YAMLFactory {

        /**
         * Instantiates a new Single quote YAML factory.
         *
         * @param builder the builder to create this object from
         */
        public SingleQuoteYAMLFactory(final @NotNull YAMLFactoryBuilder builder) {
            super(builder);
        }

        @Override
        protected YAMLGenerator _createGenerator(final Writer out,
                                                 final IOContext context) throws IOException {
            int feats = _yamlGeneratorFeatures;
            if (_dumperOptions == null) {
                return new SingleQuoteYAMLGenerator(context, _generatorFeatures, feats,
                        _quotingChecker, _objectCodec, out, _version);
            } else {
                return new SingleQuoteYAMLGenerator(context, _generatorFeatures, feats,
                        _quotingChecker, _objectCodec, out, _dumperOptions);
            }
        }

    }

    /**
     * A special {@link YAMLGenerator} that writes {@link String} values
     * in {@link DumperOptions.ScalarStyle#SINGLE_QUOTED} format.
     */
    static class SingleQuoteYAMLGenerator extends YAMLGenerator {

        /**
         * Instantiates a new Single quote yaml generator.
         *
         * @param context        the context
         * @param jsonFeatures   the JSON features
         * @param yamlFeatures   the YAML features
         * @param quotingChecker the quoting checker
         * @param codec          the codec
         * @param out            the out
         * @param version        the version
         * @throws IOException the io exception
         */
        public SingleQuoteYAMLGenerator(final IOContext context,
                                        final int jsonFeatures,
                                        final int yamlFeatures,
                                        final StringQuotingChecker quotingChecker,
                                        final ObjectCodec codec,
                                        final Writer out,
                                        final DumperOptions.Version version) throws IOException {
            super(context, jsonFeatures, yamlFeatures, quotingChecker, codec, out, version);
        }

        /**
         * Instantiates a new Single quote yaml generator.
         *
         * @param context        the context
         * @param jsonFeatures   the JSON features
         * @param yamlFeatures   the YAML features
         * @param quotingChecker the quoting checker
         * @param codec          the codec
         * @param out            the out
         * @param dumperOptions  the dumper options
         * @throws IOException the io exception
         */
        public SingleQuoteYAMLGenerator(final IOContext context,
                                        final int jsonFeatures,
                                        final int yamlFeatures,
                                        final StringQuotingChecker quotingChecker,
                                        final ObjectCodec codec,
                                        final Writer out,
                                        final DumperOptions dumperOptions) throws IOException {
            super(context, jsonFeatures, yamlFeatures, quotingChecker, codec, out, dumperOptions);
        }

        @Override
        protected void _writeScalar(final @NotNull String value,
                                    final @NotNull String type,
                                    @NotNull DumperOptions.ScalarStyle style) throws IOException {
            if (type.equals("string") && style == DumperOptions.ScalarStyle.DOUBLE_QUOTED)
                style = DumperOptions.ScalarStyle.SINGLE_QUOTED;
            super._writeScalar(value, type, style);
        }

    }

}
