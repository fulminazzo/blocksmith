package it.fulminazzo.blacksmith.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.fulminazzo.blacksmith.config.ConfigurationAdapter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * A special implementation of {@link ConfigurationAdapter}
 * that uses the <a href="https://github.com/FasterXML/jackson">jackson project</a>
 * for serialization and deserialization.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class JacksonConfigurationAdapter implements ConfigurationAdapter {

    @NotNull ObjectMapper mapper;

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void store(final @NotNull T configuration, final @NotNull File file) {
        throw new UnsupportedOperationException();
    }

}
