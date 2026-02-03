package it.fulminazzo.blacksmith.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A special type of {@link MapDeserializer} that will remove
 * any <code>null</code> key from the deserialization result.
 * <br>
 * Works in pair with {@link LoggerDeserializationProblemHandler} to prevent
 * deserialization errors.
 */
final class NonNullKeyMapDeserializer extends MapDeserializer {

    /**
     * Instantiates a new Lenient map deserializer.
     *
     * @param delegate the delegate deserializer
     */
    public NonNullKeyMapDeserializer(final @NotNull MapDeserializer delegate) {
        super(delegate);
    }

    @Override
    protected MapDeserializer withResolved(final KeyDeserializer keyDeserializer,
                                           final TypeDeserializer valueTypeDeserializer,
                                           final JsonDeserializer<?> valueDeserializer,
                                           final NullValueProvider nuller,
                                           final Set<String> ignorable,
                                           final Set<String> includable) {
        return new NonNullKeyMapDeserializer(super.withResolved(
                keyDeserializer,
                valueTypeDeserializer,
                valueDeserializer,
                nuller,
                ignorable,
                includable
        ));
    }

    @Override
    public Map<Object, Object> deserialize(final JsonParser parser,
                                           final DeserializationContext context) throws IOException {
        return cleanupMap(super.deserialize(parser, context));
    }

    @Override
    public Map<Object, Object> deserialize(final JsonParser parser,
                                           final DeserializationContext context,
                                           final Map<Object, Object> result) throws IOException {
        return cleanupMap(super.deserialize(parser, context, result));
    }

    /**
     * Removes any <code>null</code> key from the given map.
     *
     * @param map the map
     * @return the given map (without the key)
     */
    static @Nullable Map<Object, Object> cleanupMap(final @Nullable Map<Object, Object> map) {
        if (map != null) {
            try {
                map.remove(null);
            } catch (UnsupportedOperationException ignored) {
                Map<Object, Object> clone = new HashMap<>();
                map.forEach((k, v) -> {
                    if (k != null) clone.put(k, v);
                });
                return Collections.unmodifiableMap(clone);
            }
        }
        return map;
    }

}
