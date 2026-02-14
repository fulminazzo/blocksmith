package it.fulminazzo.blocksmith.data.mapper;

import org.jetbrains.annotations.NotNull;

public class AbstractMapper implements Mapper {

    @Override
    public <T> @NotNull String serialize(final @NotNull T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> @NotNull T deserialize(final @NotNull String serialized,
                                      final @NotNull Class<T> dataType) {
        throw new UnsupportedOperationException();
    }

}
