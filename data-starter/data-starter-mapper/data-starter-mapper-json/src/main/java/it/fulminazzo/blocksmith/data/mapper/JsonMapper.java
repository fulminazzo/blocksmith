package it.fulminazzo.blocksmith.data.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

/**
 * A basic implementation of {@link Mapper} that uses JSON.
 */
final class JsonMapper implements Mapper {
    private final @NotNull ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> @NotNull String serialize(final @NotNull T data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new MapperException(e);
        }
    }

    @Override
    public <T> @NotNull T deserialize(final @NotNull String serialized,
                                      final @NotNull Class<T> dataType) {
        try {
            return mapper.readValue(serialized, dataType);
        } catch (JsonProcessingException e) {
            throw new MapperException(e);
        }
    }

}
