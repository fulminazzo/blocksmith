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
    private static final Set<Class<?>> WRAPPER_TYPES = Set.of(
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class
    );

    /**
     * Checks if a class is a primitive or a wrapper class.
     *
     * @param clazz the class to check
     * @return the <code>true</code> if it is
     */
    public static boolean isPrimitiveOrWrapper(final @NotNull Class<?> clazz) {
        return clazz.isPrimitive() || WRAPPER_TYPES.contains(clazz);
    }

    /**
     * Returns all the fields in the given class (or superclasses)
     * that are not static.
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
