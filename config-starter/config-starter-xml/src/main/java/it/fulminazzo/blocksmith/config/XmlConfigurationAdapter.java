package it.fulminazzo.blocksmith.config;

import com.ctc.wstx.stax.WstxInputFactory;
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
import it.fulminazzo.blocksmith.naming.CaseConverter;
import it.fulminazzo.blocksmith.naming.Convention;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BaseConfigurationAdapter} for XML.
 */
final class XmlConfigurationAdapter implements BaseConfigurationAdapter {
    private static final @NotNull Convention xmlNamingConvention = Convention.PASCAL_CASE;

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

    @Override
    public @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> loadComments(final @NotNull InputStream stream) throws IOException {
        try {
            return toCommentedMap(stream);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static @NotNull Map<String, List<String>> toCommentedMap(final @NotNull InputStream stream) throws XMLStreamException {
        final XMLInputFactory factory = new WstxInputFactory();
        final XMLStreamReader reader = factory.createXMLStreamReader(stream);

        boolean isRoot = true;
        Map<String, List<String>> result = new LinkedHashMap<>();
        Deque<String> path = new ArrayDeque<>();
        Deque<Map<String, Integer>> childCounts = new ArrayDeque<>();
        Deque<Boolean> isCollection = new ArrayDeque<>();
        List<String> pending = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamConstants.COMMENT: {
                    for (String line : reader.getText().split("\\r?\\n")) {
                        String t = line.trim();
                        if (!t.isEmpty()) pending.add(t);
                    }
                    break;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    String name = reader.getLocalName();
                    boolean parentIsCollection = !isCollection.isEmpty() && isCollection.peek();

                    if (!childCounts.isEmpty()) {
                        int count = childCounts.peek().merge(name, 1, Integer::sum);
                        if (count == 2) {
                            isCollection.pop();
                            isCollection.push(true);
                            parentIsCollection = true;
                            String parentPath = dotPath(path);
                            result.remove(parentPath.isEmpty() ? name : parentPath + "." + name);
                        }
                    }

                    if (isRoot) isRoot = false;
                    else path.push(name);
                    childCounts.push(new HashMap<>());
                    isCollection.push(false);

                    if (!parentIsCollection && !pending.isEmpty())
                        result.computeIfAbsent(dotPath(path), k -> new ArrayList<>()).addAll(pending);
                    pending.clear();
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    if (!path.isEmpty()) path.pop();
                    childCounts.pop();
                    isCollection.pop();
                    pending.clear();
                }
            }
        }

        reader.close();
        return result;
    }

    private static String dotPath(final @NotNull Deque<String> path) {
        List<String> parts = new ArrayList<>(path);
        Collections.reverse(parts);
        return parts.stream()
                .map(p -> CaseConverter.convert(p, xmlNamingConvention, JacksonConfigurationAdapter.javaNamingConvention))
                .collect(Collectors.joining("."));
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
            return CaseConverter.convert(propertyName, xmlNamingConvention);
        }

    }

}
