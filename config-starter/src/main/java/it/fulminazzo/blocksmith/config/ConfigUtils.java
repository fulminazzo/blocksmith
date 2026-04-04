package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper functions for this package.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ConfigUtils {

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
            Object value = entry.getValue();

            if (value instanceof Map<?, ?>) {
                Map<String, Object> subConfiguration = (Map<String, Object>) value;
                value = mergeDataMaps(subConfiguration, comments.containsKey(key)
                        ? (Map<String, Object>) comments.get(key)
                        : Collections.emptyMap()
                );
            }

            if (comments.containsKey(key))
                result.put(new CommentKey(key, (List<String>) comments.get(key)), value);
            else result.put(new CommentKey(key), value);
        }
        return result;
    }

}
