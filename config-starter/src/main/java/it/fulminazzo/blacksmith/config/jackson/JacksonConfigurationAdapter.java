package it.fulminazzo.blacksmith.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.fulminazzo.blacksmith.config.ConfigurationAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * A special implementation of {@link ConfigurationAdapter}
 * that uses the <a href="https://github.com/FasterXML/jackson">jackson project</a>
 * for serialization and deserialization.
 */
final class JacksonConfigurationAdapter implements ConfigurationAdapter {
    private final @NotNull ObjectMapper mapper;

    /**
     * Instantiates a new Jackson configuration adapter.
     *
     * @param mapper the object mapper
     * @param logger the logger
     */
    public JacksonConfigurationAdapter(final @NotNull ObjectMapper mapper,
                                       final @NotNull Logger logger) {
        this.mapper = JacksonUtils.setupMapper(mapper, logger);
    }

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException {
        return mapper.readValue(file, type);
    }

    @Override
    public <T> void store(final @NotNull T configuration, final @NotNull File file) {
        throw new UnsupportedOperationException();
    }

}
