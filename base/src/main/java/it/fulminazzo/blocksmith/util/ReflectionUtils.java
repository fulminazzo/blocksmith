package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A collection of utilities to work with reflections.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    /**
     * Returns the field with the name in the given class (or superclasses) that is not static.
     *
     * @param container the container
     * @param fieldName the field name
     * @return the field (if found)
     */
    public static @NotNull Optional<Field> getInstanceField(final @NotNull Class<?> container,
                                                            final @NotNull String fieldName) {
        Class<?> curr = container;
        while (curr != null && !curr.equals(Object.class)) {
            try {
                Field field = curr.getDeclaredField(fieldName);
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                    return Optional.of(field);
            } catch (NoSuchFieldException ignored) {
            }
            curr = curr.getSuperclass();
        }
        return Optional.empty();
    }

    /**
     * Returns all the fields in the given class (or superclasses) that are not static.
     *
     * @param container the container of the fields
     * @return the fields
     */
    public static @NotNull Collection<Field> getInstanceFields(final @NotNull Class<?> container) {
        List<Field> fields = new ArrayList<>();
        Class<?> curr = container;
        while (curr != null && !curr.equals(Object.class)) {
            fields.addAll(Arrays.stream(curr.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers()))
                    .collect(Collectors.toList()));
            curr = curr.getSuperclass();
        }
        return fields;
    }

}
