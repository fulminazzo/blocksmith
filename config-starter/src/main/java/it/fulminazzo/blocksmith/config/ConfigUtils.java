package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Helper functions for this package.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ConfigUtils {
    private static final @NotNull List<Function<String, Object>> primitiveConverters = List.of(
            Byte::valueOf,
            Short::valueOf,
            Integer::valueOf,
            Long::valueOf,
            Float::valueOf,
            Double::valueOf,
            s -> {
                if (s.length() == 1) return s.charAt(0);
                else throw new IllegalArgumentException(s + " is not a valid character");
            },
            s -> {
                if (s.equals(Boolean.TRUE.toString())) return true;
                else if (s.equals(Boolean.FALSE.toString())) return false;
                else throw new IllegalArgumentException(s + " is not a valid boolean");
            }
    );

    /**
     * Joins the given configuration and comments into a single map.
     *
     * @param configuration the configuration
     * @param comments      the comments
     * @return the joined data
     */
    @SuppressWarnings("unchecked")
    public static @NotNull Map<@NotNull CommentKey, @Nullable Object> mergeDataMaps(
            final @NotNull Map<@NotNull String, @Nullable Object> configuration,
            final @NotNull Map<@NotNull String, @NotNull Object> comments
    ) {
        final Map<CommentKey, Object> result = new HashMap<>();
        for (final Map.Entry<String, Object> entry : configuration.entrySet()) {
            final String key = entry.getKey();
            Object value = convertValue(entry.getValue());
            final Object comment = comments.get(key);

            if (value instanceof Map<?, ?>) {
                Map<String, Object> subConfiguration = (Map<String, Object>) value;
                value = mergeDataMaps(subConfiguration, comment instanceof Map
                        ? (Map<String, Object>) comments.get(key)
                        : Collections.emptyMap()
                );
            }

            if (comment instanceof List<?>) result.put(new CommentKey(key, (List<String>) comment), value);
            else result.put(new CommentKey(key), value);
        }
        return result;
    }

    private static @Nullable Object convertValue(final @Nullable Object value) {
        if (value == null) return null;
        for (Function<String, Object> converter : primitiveConverters) {
            try {
                return converter.apply(value.toString());
            } catch (Exception ignored) {
            }
        }
        return value;
    }

}
