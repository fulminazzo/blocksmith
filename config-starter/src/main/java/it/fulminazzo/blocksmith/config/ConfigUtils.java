package it.fulminazzo.blocksmith.config;

import it.fulminazzo.blocksmith.naming.CaseConverter;
import it.fulminazzo.blocksmith.naming.Convention;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Helper functions for this package.
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigUtils {
    /**
     * Java default naming convention for properties (fields).
     */
    public static final @NotNull Convention javaNamingConvention = Convention.CAMEL_CASE;

    private static final @NotNull List<Function<String, Object>> primitiveConverters = List.of(
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
    static @NotNull Map<@NotNull CommentKey, @Nullable Object> mergeDataMaps(
            final @NotNull Map<@NotNull String, @Nullable Object> configuration,
            final @NotNull Map<@NotNull String, @NotNull Object> comments
    ) {
        final Map<CommentKey, Object> result = new LinkedHashMap<>();
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

    /**
     * Checks if the given configuration is a {@link Map}.
     * If it is, its keys are converted based on the given {@link Convention}s.
     *
     * @param <T>           the type of the configuration
     * @param configuration the configuration
     * @param from          the convention to convert from
     * @param to            the convention to convert to
     * @return the updated configuration
     */
    static <T> @NotNull T checkMap(final @NotNull T configuration,
                                   final @NotNull Convention from,
                                   final @NotNull Convention to) {
        try {
            return (T) convertKeysFormat((Map<String, ?>) configuration, from, to);
        } catch (ClassCastException e) {
            return configuration;
        }
    }

    private static @NotNull Map<String, Object> convertKeysFormat(final @NotNull Map<String, ?> configuration,
                                                                  final @NotNull Convention from,
                                                                  final @NotNull Convention to) {
        final Map<String, Object> converted = new HashMap<>();
        for (final Map.Entry<String, ?> entry : configuration.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>)
                value = convertKeysFormat((Map<String, ?>) value, from, to);
            converted.put(
                    CaseConverter.convert(key, from, to),
                    value
            );
        }
        return converted;
    }

}
