package it.fulminazzo.blocksmith.data.mapper;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Base64;

/**
 * A basic implementation of {@link Mapper} for {@link java.io.Serializable} objects.
 */
final class SerializableMapper implements Mapper {

    @Override
    public @NotNull <T> String serialize(final @NotNull T data) {
        try (
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(output)
        ) {
            objectStream.writeObject(data);
            byte[] raw = output.toByteArray();
            return Base64.getEncoder().encodeToString(raw);
        } catch (IOException e) {
            throw new MapperException(e);
        }
    }

    @Override
    public @NotNull <T> T deserialize(final @NotNull String serialized,
                                      final @NotNull Class<T> dataType) {
        byte[] raw = Base64.getDecoder().decode(serialized);
        try (
                ByteArrayInputStream input = new ByteArrayInputStream(raw);
                ObjectInputStream inputStream = new ObjectInputStream(input)
        ) {
            return dataType.cast(inputStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new MapperException(e);
        }
    }

}
