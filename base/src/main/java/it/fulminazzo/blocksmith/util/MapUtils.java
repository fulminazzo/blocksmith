package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Collection of utilities to work with maps.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapUtils {

    /**
     * Flattens a nested {@link Map} into a single-level map using dot-notation keys.
     * Keys <b>must not</b> be <code>null</code>.
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
     *   "players": ["Steve", "Alex"]
     * }
     * }</pre>
     *
     * @param map the map to flatten
     * @return the flattened map
     */
    public static @NotNull Map<@NotNull String, @Nullable Object> flatten(final @NotNull Map<@NotNull String, @Nullable Object> map) {
        final Map<String, Object> flattened = new HashMap<>();
        for (Map.Entry<@NotNull String, @Nullable Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                Map<String, Object> stringObjectMap = toStringKeyMap((Map<?, ?>) value);
                flatten(stringObjectMap).forEach((k, v) -> flattened.put(key + "." + k, v));
            } else flattened.put(key, value);
        }
        return flattened;
    }

    /**
     * Recomputes the key-values of the given map to remove any dot-notation key.
     * Keys <b>must not</b> be <code>null</code>.
     * <br>
     * For example, a structure such as:
     * <pre>{@code
     * {
     *   "player.join": "Welcome!",
     *   "player.leave": "Goodbye!",
     *   "players": ["Steve", "Alex"]
     * }
     * }</pre>
     * would be converted to:
     * <pre>{@code
     * {
     *   "player": {
     *     "join": "Welcome!",
     *     "leave": "Goodbye!"
     *   },
     *   "players": ["Steve", "Alex"]
     * }
     * }</pre>
     */
    public static @NotNull Map<@NotNull String, @Nullable Object> unflatten(final @NotNull Map<@NotNull String, @Nullable Object> map) {
        final Map<String, Object> unflattened = new HashMap<>();
        final Set<String> toUnflatten = new HashSet<>();
        for (Map.Entry<@NotNull String, @Nullable Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.contains(".")) toUnflatten.add(key.substring(0, key.indexOf(".")));
            else unflattened.put(key, value);
        }
        while (!toUnflatten.isEmpty()) {
            String key = toUnflatten.iterator().next();
            toUnflatten.remove(key);
            Map<String, Object> subMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String subKey = entry.getKey();
                if (subKey.startsWith(key + "."))
                    subMap.put(subKey.substring(key.length() + 1), entry.getValue());
            }
            unflattened.put(key, unflatten(subMap));
        }
        return unflattened;
    }

    /**
     * Converts the given object accordingly.
     * <br>
     * If the object is an array, it is converted to a {@link Collection}.
     * The same conversion is done recursively for each element.
     * If the object is not an array, it is returned as is.
     *
     * @param object the object
     * @return the potentially converted object
     */
    static Object convertArray(final @Nullable Object object) {
        if (object == null) return null;
        else if (object.getClass().isArray()) {
            Object[] array = (Object[]) object;
            for (int i = 0; i < array.length; i++) array[i] = convertArray(array[i]);
            return Arrays.asList(array);
        } else return object;
    }

    private static @NotNull Map<String, Object> toStringKeyMap(final @NotNull Map<?, ?> map) {
        final Map<String, Object> stringKeyMap = new HashMap<>();
        map.forEach((k, v) -> stringKeyMap.put(k.toString(), v));
        return stringKeyMap;
    }

}
