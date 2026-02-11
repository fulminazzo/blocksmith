package it.fulminazzo.blocksmith.data.mapper;

import org.jetbrains.annotations.NotNull;

/**
 * A mapper supports serialization and deserialization for a general Java bean.
 */
public interface Mapper {

    /**
     * Serializes an object to a string.
     *
     * @param <T>  the type of the data
     * @param data the data
     * @return the string
     */
    <T> @NotNull String serialize(final @NotNull T data);

    /**
     * Deserializes a string into an object.
     *
     * @param <T>        the type of the data
     * @param serialized the serialized
     * @return the data
     */
    <T> @NotNull T deserialize(final @NotNull String serialized,
                               final @NotNull Class<T> dataType);

}
