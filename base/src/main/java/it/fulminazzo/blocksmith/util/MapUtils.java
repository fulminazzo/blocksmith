package it.fulminazzo.blocksmith.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joor.Reflect;

import java.lang.reflect.Field;
import java.util.*;

/**
 * A collection of utilities to work with Map objects.
 */
public final class MapUtils {

    /**
     * Flattens a nested {@link Map} into a single-level map using dot-notation keys.
     * <p>
     * Nested maps are recursively traversed, with each level's key appended to the
     * parent key separated by a dot. For example, a nested structure such as:
     * <pre>{@code
     * {
     *   "player": {
     *     "join": "Welcome!",
     *     "leave": "Goodbye!"
     *   },
     *   "players": ["Steve", "Alex"]
     * }
     * }</pre>
     * would be flattened to:
     * <pre>{@code
     * {
     *   "player.join": "Welcome!",
     *   "player.leave": "Goodbye!",
     *   "players[0]": "Steve",
     *   "players[1]": "Alex"
     * }
     * }</pre>
     *
     * @param map the map to flatten
     * @return the flattened map
     */
    public static @NotNull Map<String, Object> flattenMap(final @NotNull Map<String, Object> map) {
        final Map<String, Object> flattened = new HashMap<>();
        for (final Map.Entry<String, Object> entry : map.entrySet())
            flattenMapRec(flattened, entry.getKey(), entry.getValue());
        return flattened;
    }

    private static void flattenMapRec(final @NotNull Map<String, Object> result,
                                      final @NotNull String key,
                                      final @Nullable Object value) {
        if (value == null || ReflectionUtils.isPrimitiveOrWrapper(value.getClass())) result.put(key, value);
        else if (value.getClass().isArray()) flattenArrayHelper(result, key, (Object[]) value);
        else if (value instanceof Collection) flattenCollectionHelper(result, key, (Collection<?>) value);
        else if (value instanceof Map) flattenMapHelper(result, key, (Map<?, ?>) value);
        else flattenObjectHelper(result, key, value);
    }

    private static void flattenArrayHelper(final @NotNull Map<String, Object> result,
                                           final @NotNull String key,
                                           final @NotNull Object[] array) {
        for (int i = 0; i < array.length; i++)
            flattenMapRec(result, key + String.format("[%s]", i), array[i]);
    }

    private static void flattenCollectionHelper(final @NotNull Map<String, Object> result,
                                                final @NotNull String key,
                                                final @NotNull Collection<?> collection) {
        List<?> tmp = new ArrayList<>(collection);
        for (int i = 0; i < tmp.size(); i++)
            flattenMapRec(result, key + String.format("[%s]", i), tmp.get(i));
    }

    private static void flattenMapHelper(final @NotNull Map<String, Object> result,
                                         final @NotNull String key,
                                         final @NotNull Map<?, ?> map) {
        for (Object k : map.keySet()) {
            String kString = k == null ? "null" : k.toString();
            flattenMapRec(result, key + "." + kString, map.get(k));
        }
    }

    private static void flattenObjectHelper(final @NotNull Map<String, Object> result,
                                            final @NotNull String key,
                                            final @NotNull Object object) {
        final Reflect reflect = Reflect.on(object);
        for (Field field : ReflectionUtils.getInstanceFields(object.getClass())) {
            String fieldName = field.getName();
            flattenMapRec(result, key + "." + fieldName, reflect.field(fieldName).get());
        }
    }

}
