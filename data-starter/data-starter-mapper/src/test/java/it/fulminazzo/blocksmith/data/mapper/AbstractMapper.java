package it.fulminazzo.blocksmith.data.mapper;

import org.jetbrains.annotations.NotNull;

public class AbstractMapper implements Mapper {

    @Override
    public @NotNull <T> String serialize(final @NotNull T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <T> T deserialize(final @NotNull String serialized,
                                      final @NotNull Class<T> dataType) {
        throw new UnsupportedOperationException();
    }

}
