package it.fulminazzo.blocksmith.util;

import it.fulminazzo.blocksmith.naming.CaseConverter;
import it.fulminazzo.blocksmith.naming.Convention;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Collection of utilities to work with maps.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapUtils {

    /**
     * Converts the naming convention of the keys to the given one.
     * <br>
     * Recursively traverses the map if any sub-map is found.
     * <br>
     * <b>WARNING</b>: assumes the internal maps have {@link String} keys.
     *
     * @param map  the map
     * @param from the naming convention of the keys
     * @param to   the naming convention to convert the keys to
     * @return the converted map
     */
    @SuppressWarnings("unchecked")
    public static @NotNull Map<@Nullable String, @Nullable Object> convertNames(
            final @NotNull Map<@Nullable String, ?> map,
            final @NotNull Convention from,
            final @NotNull Convention to
    ) {
        Map<@Nullable String, @Nullable Object> converted = new HashMap<>();
        for (Map.Entry<@Nullable String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key != null) key = CaseConverter.convert(key, from, to);
            if (value instanceof Map<?, ?>)
                value = convertNames((Map<String, Object>) value, from, to);
            converted.put(key, value);
        }
        return converted;
    }

    /**
     * Converts the given map to a map of string keys and string values.
     * <br>
     * {@link Map} instances are flattened using {@link #flatten(Map)}.
     * {@link Collection} instances are merged in a single string separated by "\n".
     *
     * @param map the map to stringify
     * @return the stringified map
     */
    public static @NotNull Map<@NotNull String, @NotNull String> stringify(final @NotNull Map<?, ?> map) {
        Map<@NotNull String, ?> transformed = toStringKeyMap(map);
        transformed = flatten(transformed);
        final Map<@NotNull String, @NotNull String> stringified = new LinkedHashMap<>();
        for (final Map.Entry<String, ?> entry : transformed.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) stringified.put(key, stringifyValue(value));
        }
        return stringified;
    }

    private static @NotNull String stringifyValue(final @NotNull Object value) {
        if (value instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) value;
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(MapUtils::stringifyValue)
                    .collect(Collectors.joining("\n"));
        } else return value.toString();
    }

    /**
     * Flattens a nested {@link Map} into a single-level map using dot-notation keys.
     * Keys <b>must not</b> be {@code null}.
     * <br>
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
    public static @NotNull Map<@NotNull String, @Nullable Object> flatten(final @NotNull Map<@NotNull String, ?> map) {
        final Map<String, Object> flattened = new HashMap<>();
        for (Map.Entry<@NotNull String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                Map<String, ?> stringObjectMap = toStringKeyMap((Map<?, ?>) value);
                flatten(stringObjectMap).forEach((k, v) -> flattened.put(key + "." + k, v));
            } else flattened.put(key, value);
        }
        return flattened;
    }

    /**
     * Recomputes the key-values of the given map to remove any dot-notation key.
     * Keys <b>must not</b> be {@code null}.
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
     *
     * @param map the map to unflatten
     * @return the unflattened map
     */
    public static @NotNull Map<@NotNull String, @Nullable Object> unflatten(
            final @NotNull Map<@NotNull String, ?> map
    ) {
        final Map<String, Object> unflattened = new HashMap<>();
        final Set<String> toUnflatten = new HashSet<>();
        for (Map.Entry<@NotNull String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.contains(".")) toUnflatten.add(key.substring(0, key.indexOf(".")));
            else unflattened.put(key, value);
        }
        while (!toUnflatten.isEmpty()) {
            String key = toUnflatten.iterator().next();
            toUnflatten.remove(key);
            Map<String, Object> subMap = new HashMap<>();
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                String subKey = entry.getKey();
                if (subKey.startsWith(key + "."))
                    subMap.put(subKey.substring(key.length() + 1), entry.getValue());
            }
            unflattened.put(key, unflatten(subMap));
        }
        return unflattened;
    }

    /**
     * Expands the collections contained in the given map.
     * Collections are either {@link Collection} instances or array objects.
     * Keys <b>must not</b> be {@code null}.
     * <br>
     * Each element of the collection is put in the map in the format
     * {@code <keyPrefix>[<index>]} where {@code <index>}
     * is the index of the element.
     *
     * @param map the map to expand
     * @return the expanded map
     */
    public static @NotNull Map<@NotNull String, @Nullable Object> expandCollections(
            final @NotNull Map<@NotNull String, ?> map
    ) {
        final Map<@NotNull String, @Nullable Object> expanded = new HashMap<>();
        for (Map.Entry<@NotNull String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = convertArray(entry.getValue());
            if (value instanceof Map<?, ?>) {
                Map<String, Object> stringObjectMap = toStringKeyMap((Map<?, ?>) value);
                expanded.put(key, expandCollections(stringObjectMap));
            } else if (value instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) value;
                expandCollection(expanded, collection, key);
            } else expanded.put(key, value);
        }
        return expanded;
    }

    /**
     * Each element of the collection is put in the map in the format
     * {@code <keyPrefix>[<index>]} where {@code <index>}
     * is the index of the element.
     * If an element is a {@link Collection}, then its elements will be
     * put instead (with the format {@code <keyPrefix>[<indexOfCollection>][<indexOfElement>]}).
     *
     * @param expanded   the map to put the elements into
     * @param collection the collection to get elements from
     * @param keyPrefix  the key prefix
     */
    static void expandCollection(
            final @NotNull Map<String, Object> expanded,
            final @NotNull Collection<?> collection,
            final @NotNull String keyPrefix
    ) {
        List<Object> list = new ArrayList<>(collection);
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            String key = keyPrefix + String.format("[%s]", i);
            if (value instanceof Map<?, ?>) {
                Map<String, Object> stringObjectMap = toStringKeyMap((Map<?, ?>) value);
                expanded.put(key, expandCollections(stringObjectMap));
            } else if (value instanceof Collection<?>)
                expandCollection(expanded, (Collection<?>) value, key);
            else expanded.put(key, value);
        }
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

    /**
     * Converts the given map to a map of {@link String} as keys.
     * Keys that are {@code null} are ignored.
     *
     * @param map the map
     * @return the converted map
     */
    public static @NotNull Map<@NotNull String, @Nullable Object> toStringKeyMap(final @NotNull Map<?, ?> map) {
        final Map<String, Object> stringKeyMap = new HashMap<>();
        map.forEach((k, v) -> {
            if (k != null) stringKeyMap.put(k.toString(), v);
        });
        return stringKeyMap;
    }

}
