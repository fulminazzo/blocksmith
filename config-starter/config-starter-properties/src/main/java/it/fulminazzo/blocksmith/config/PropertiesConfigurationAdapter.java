package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import it.fulminazzo.blocksmith.config.jackson.CommentPropertyWriter;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Implementation of {@link BaseConfigurationAdapter} for Properties.
 */
final class PropertiesConfigurationAdapter implements BaseConfigurationAdapter {
    private static final @NotNull List<String> commentIdentifiers = Arrays.asList("#", "!");

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

    @Override
    public @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> loadComments(final @NotNull InputStream stream) throws IOException {
        return toCommentedMap(stream);
    }

    @Override
    public <T> @NotNull T load(final @NotNull String data, final @NotNull Class<T> type) throws IOException {
        return delegate.load(data, type);
    }

    @Override
    public <T> @NotNull T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        return delegate.load(file, type);
    }

    @Override
    public <T> @NotNull T load(final @NotNull InputStream stream, final @NotNull Class<T> type) throws IOException {
        return delegate.load(stream, type);
    }

    @Override
    public <T> @NotNull String serialize(final @NotNull T configuration) throws IOException {
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

    private static @NotNull Map<String, List<String>> toCommentedMap(final @NotNull InputStream inputStream) throws IOException {
        final Map<String, List<String>> keysComments = new HashMap<>();
        List<String> currentComment = new ArrayList<>();
        try (InputStreamReader streamReader = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String identifier = getCommentIdentifier(line);
                if (identifier != null) currentComment.add(line.substring(identifier.length()).trim());
                else if (!currentComment.isEmpty()) {
                    String key = line.split("=")[0].trim();
                    int index = key.lastIndexOf('.');
                    if (index != -1) {
                        String keyName = key.substring(index + 1);
                        if (keyName.chars().allMatch(Character::isDigit)) {
                            // ignoring lists as comments are not supported
                            currentComment.clear();
                            continue;
                        }
                    }
                    keysComments.put(key, currentComment);
                    currentComment = new ArrayList<>();
                }
            }
        }
        return keysComments;
    }

    private static @Nullable String getCommentIdentifier(final @NotNull String line) {
        for (String identifier : commentIdentifiers)
            if (line.startsWith(identifier))
                return identifier;
        return null;
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
