package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.fulminazzo.blocksmith.config.BaseConfigurationAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A special implementation of {@link BaseConfigurationAdapter}
 * that uses the <a href="https://github.com/FasterXML/jackson">jackson project</a>
 * for serialization and deserialization.
 */
public final class JacksonConfigurationAdapter implements BaseConfigurationAdapter {
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
                                       final @Nullable Class<? extends CommentPropertyWriter> commentPropertyWriterType) {
        this.mapper = JacksonUtils.setupMapper(mapper, logger, commentPropertyWriterType);
    }

    @Override
    public <T> @NotNull T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        return mapper.readValue(file, type);
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) throws IOException {
        Files.createDirectories(file.getParentFile().toPath());
        mapper.writeValue(file, configuration);
    }

}
