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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.events.CommentEvent;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BaseConfigurationAdapter} for YAML.
 */
final class YamlConfigurationAdapter implements BaseConfigurationAdapter {
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

    @Override
    public @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> loadComments(final @NotNull String data) {
        return loadComments(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> loadComments(final @NotNull File file) throws IOException {
        return loadComments(new FileInputStream(file));
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> loadComments(final @NotNull InputStream stream) {
        final LoaderOptions options = new LoaderOptions().setProcessComments(true);
        StreamReader reader = new StreamReader(new InputStreamReader(stream));
        Composer composer = new Composer(new ParserImpl(reader, options), new Resolver(), options);
        Node root = composer.getSingleNode();
        if (root instanceof MappingNode) return extractComments((MappingNode) root);
        else return Collections.emptyMap();
    }

    private static @NotNull Map<String, List<String>> extractComments(final @NotNull MappingNode node) {
        final Map<String, List<String>> nodesComments = new HashMap<>();
        for (NodeTuple nodeTuple : node.getValue()) {
            ScalarNode keyNode = (ScalarNode) nodeTuple.getKeyNode();
            String key = keyNode.getValue();
            @NotNull List<String> comments = extractComments(keyNode);
            if (!comments.isEmpty()) nodesComments.put(key, comments);
            Node value = nodeTuple.getValueNode();
            if (value instanceof MappingNode)
                extractComments((MappingNode) value).forEach((k, c) ->
                        nodesComments.put(key + "." + k, c)
                );
        }
        return nodesComments;
    }

    private static @NotNull List<String> extractComments(final @NotNull Node node) {
        List<CommentLine> comments = new ArrayList<>();
        List<CommentLine> inlineComments = node.getInLineComments();
        if (inlineComments != null) comments.addAll(inlineComments);
        List<CommentLine> blockComments = node.getBlockComments();
        if (blockComments != null) comments.addAll(blockComments);
        return comments.stream()
                .map(CommentLine::getValue)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull <T> T load(final @NotNull String data, final @NotNull Class<T> type) throws IOException {
        return delegate.load(data, type);
    }

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        return delegate.load(file, type);
    }

    @Override
    public @NotNull <T> T load(final @NotNull InputStream stream, final @NotNull Class<T> type) throws IOException {
        return delegate.load(stream, type);
    }

    @Override
    public @NotNull <T> T loadFromResource(final @NotNull String resource, final @NotNull Class<T> type) throws IOException {
        return delegate.loadFromResource(resource, type);
    }

    @Override
    public @NotNull <T> String serialize(final @NotNull T configuration) throws IOException {
        return delegate.serialize(configuration);
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) throws IOException {
        delegate.store(file, configuration);
    }

    @Override
    public <T> void store(final @NotNull OutputStream stream, final @NotNull T configuration) throws IOException {
        delegate.store(stream, configuration);
    }

    @Override
    public @NotNull <T> T extractAndLoad(final @NotNull String resource,
                                         final @NotNull File directory,
                                         final @NotNull Class<T> type) throws IOException {
        return delegate.extractAndLoad(resource, directory, type);
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

        private SingleQuoteYAMLFactory(final @NotNull SingleQuoteYAMLFactory singleQuoteYAMLFactory) {
            super(singleQuoteYAMLFactory, null);
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

        @Override
        public YAMLFactory copy() {
            return new SingleQuoteYAMLFactory(this);
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
