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
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            double.class, Double.class,
            float.class, Float.class,
            int.class, Integer.class,
            long.class, Long.class,
            short.class, Short.class
    );

    /**
     * Checks if the specified class is available on runtime.
     *
     * @param className the class name
     * @return <code>true</code> if it is
     */
    public static boolean isClassAvailable(final @NotNull String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Converts the given class to a wrapper type.
     *
     * @param type the primitive class
     * @return the wrapper class
     */
    public static @NotNull Class<?> toWrapper(final @NotNull Class<?> type) {
        return PRIMITIVE_TO_WRAPPER.getOrDefault(type, type);
    }

    /**
     * Checks if a class is a primitive or a wrapper class.
     *
     * @param type the class to check
     * @return <code>true</code> if it is
     */
    public static boolean isPrimitiveOrWrapper(final @NotNull Class<?> type) {
        return type.isPrimitive() || WRAPPER_TYPES.contains(type);
    }

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
