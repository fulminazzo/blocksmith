package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * A special implementation of {@link ConfigurationAdapter}
 * that uses the <a href="https://github.com/FasterXML/jackson">jackson project</a>
 * for serialization and deserialization.
 */
public final class JacksonConfigurationAdapter implements ConfigurationAdapter {
    private final @NotNull ObjectMapper mapper;

    /**
     * Instantiates a new Jackson configuration adapter.
     *
     * @param mapper                    the object mapper
     * @param logger                    the logger
     * @param commentPropertyWriterType the type of {@link CommentPropertyWriter} responsible for writing comments
     */
    public JacksonConfigurationAdapter(final @NotNull ObjectMapper mapper,
                                       final @NotNull Logger logger,
                                       final @NotNull Class<? extends CommentPropertyWriter> commentPropertyWriterType) {
        this.mapper = JacksonUtils.setupMapper(mapper, logger, commentPropertyWriterType);
    }

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        return mapper.readValue(file, type);
    }

    @Override
    public <T> void store(final @NotNull T configuration, final @NotNull File file) throws IOException {
        mapper.writeValue(file, configuration);
    }

}
