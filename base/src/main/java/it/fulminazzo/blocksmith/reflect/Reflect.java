package it.fulminazzo.blocksmith.reflect;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A wrapper for Java objects to work with reflections.
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Reflect {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class,
            boolean.class, Boolean.class
    );

    @NotNull Class<?> type;
    @NotNull Object object;

    /*
     * CLASS
     */

    /**
     * Checks if the type is primitive.
     *
     * @return <code>true</code> if it is
     */
    public boolean isPrimitive() {
        return type.isPrimitive();
    }

    /**
     * Checks if the type is a Java wrapper type.
     *
     * @return <code>true</code> if it is
     */
    public boolean isWrapper() {
        return PRIMITIVE_TO_WRAPPER.containsValue(type);
    }

    /**
     * Checks if the type is a base Java type.
     * A base type is a primitive, wrapper or {@link String}.
     *
     * @return <code>true</code> if it is
     */
    public boolean isBaseType() {
        return isPrimitive() || isWrapper() || type.equals(String.class);
    }

    /**
     * Checks if this class extends the given type.
     *
     * @param type the type to check
     * @return <code>true</code> if it does
     */
    public boolean extendsType(final @NotNull Class<?> type) {
        return type.isAssignableFrom(this.type);
    }

    /**
     * Converts the internal type to a Java wrapper type (if the type is primitive).
     *
     * @return the reflect with the new object
     */
    public @NotNull Reflect toWrapper() {
        if (isPrimitive()) {
            Class<?> newType = PRIMITIVE_TO_WRAPPER.get(type);
            final Object newObject;
            if (object instanceof Class<?>) newObject = type;
            else newObject = newType.cast(object);
            return new Reflect(newType, newObject);
        }
        return this;
    }

    /**
     * Converts the internal type to a Java primitive type (if the type is a wrapper).
     *
     * @return the reflect with the new object
     */
    public @NotNull Reflect toPrimitive() {
        if (isWrapper()) {
            Class<?> newType = PRIMITIVE_TO_WRAPPER.entrySet().stream()
                    .filter(e -> e.getValue().equals(type))
                    .map(Map.Entry::getKey)
                    .findFirst().orElseThrow(); // should never happen
            final Object newObject;
            if (object instanceof Class<?>) newObject = type;
            else newObject = newType.cast(object);
            return new Reflect(newType, newObject);
        }
        return this;
    }

    /*
     * FIELDS
     */

    /**
     * Gets the value of all the instance fields (fields must not be static).
     *
     * @return the values of the fields
     */
    public @NotNull List<Reflect> getInstanceFieldsObject() {
        return getFieldsObject(f -> !Modifier.isStatic(f.getModifiers()));
    }

    /**
     * Gets the values of all the static fields.
     *
     * @return the values of the fields
     */
    public @NotNull List<Reflect> getStaticFieldsObject() {
        return getFieldsObject(f -> Modifier.isStatic(f.getModifiers()));
    }

    /**
     * Gets the values of all the fields.
     *
     * @return the values of the fields
     */
    public @NotNull List<Reflect> getFieldsObject() {
        return getFieldsObject(f -> true);
    }

    /**
     * Gets the values of all the fields that match the predicate.
     *
     * @param predicate the predicate
     * @return the values of the fields
     */
    public @NotNull List<Reflect> getFieldsObject(final @NotNull Predicate<Field> predicate) {
        return getFields(predicate).stream().map(this::getFieldObject).collect(Collectors.toList());
    }

    /**
     * Gets the value of the instance field with the given name (field must not be static).
     *
     * @param name the name of the field
     * @return the value of the field
     * @throws ReflectException if no field is found or an error occurs while getting the value
     */
    public @NotNull Reflect getInstanceFieldObject(final @NotNull String name) {
        return getFieldObject(f -> !Modifier.isStatic(f.getModifiers()) && f.getName().equals(name));
    }

    /**
     * Gets the value of the static field with the given name.
     *
     * @param name the name of the field
     * @return the value of the field
     * @throws ReflectException if no field is found or an error occurs while getting the value
     */
    public @NotNull Reflect getStaticFieldObject(final @NotNull String name) {
        return getFieldObject(f -> Modifier.isStatic(f.getModifiers()) && f.getName().equals(name));
    }

    /**
     * Gets the value of the field with the given name.
     *
     * @param name the name of the field
     * @return the value of the field
     * @throws ReflectException if no field is found or an error occurs while getting the value
     */
    public @NotNull Reflect getFieldObject(final @NotNull String name) {
        return getFieldObject(f -> f.getName().equals(name));
    }

    /**
     * Gets the value of the first field that matches the predicate.
     *
     * @param predicate the predicate
     * @return the value of the field
     * @throws ReflectException if no field is found or an error occurs while getting the value
     */
    public @NotNull Reflect getFieldObject(final @NotNull Predicate<Field> predicate) {
        Field field = getField(predicate);
        return getFieldObject(field);
    }

    /**
     * Gets the value of the given field.
     *
     * @param field the field
     * @return the value of the field
     * @throws ReflectException if an error occurs while getting the value
     */
    public @NotNull Reflect getFieldObject(Field field) {
        try {
            field.setAccessible(true);
            return new Reflect(field.getType(), field.get(object));
        } catch (IllegalAccessException e) {
            throw new ReflectException(e, "Could not get value of field '%s' from %s", field.getName(), object);
        }
    }

    /**
     * Gets the instance field with the given name (field must not be static).
     *
     * @param name the name of the field
     * @return the field
     * @throws ReflectException if no field is found
     */
    public @NotNull Field getInstanceField(final @NotNull String name) {
        return getField(f -> !Modifier.isStatic(f.getModifiers()) && f.getName().equals(name));
    }

    /**
     * Gets the static field with the given name.
     *
     * @param name the name of the field
     * @return the field
     * @throws ReflectException if no field is found
     */
    public @NotNull Field getStaticField(final @NotNull String name) {
        return getField(f -> Modifier.isStatic(f.getModifiers()) && f.getName().equals(name));
    }

    /**
     * Gets the field with the given name.
     *
     * @param name the name of the field
     * @return the field
     * @throws ReflectException if no field is found
     */
    public @NotNull Field getField(final @NotNull String name) {
        try {
            return getField(f -> f.getName().equals(name));
        } catch (ReflectException e) {
            throw ReflectException.cannotFindField(type, name);
        }
    }

    /**
     * Gets the first field that matches the predicate.
     *
     * @param predicate the predicate
     * @return the field
     * @throws ReflectException if no field is found
     */
    public @NotNull Field getField(final @NotNull Predicate<Field> predicate) {
        return getFields().stream().filter(predicate).findFirst().orElseThrow(() -> ReflectException.cannotFindField(type));
    }

    /**
     * Gets all the instance fields (fields not declared as static).
     *
     * @return the fields
     */
    public @NotNull List<Field> getInstanceFields() {
        return getFields(f -> !Modifier.isStatic(f.getModifiers()));
    }

    /**
     * Gets all the static fields.
     *
     * @return the static fields
     */
    public @NotNull List<Field> getStaticFields() {
        return getFields(f -> Modifier.isStatic(f.getModifiers()));
    }

    /**
     * Gets all the fields that match the predicate.
     *
     * @param predicate the predicate
     * @return the fields
     */
    public @NotNull List<Field> getFields(final @NotNull Predicate<Field> predicate) {
        return getFields().stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Gets all the fields.
     *
     * @return the fields
     */
    public @NotNull List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        Class<?> type = this.type;
        while (type != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }

    /*
     * INITIALIZERS
     */

    /**
     * Instantiates a new Reflect object.
     *
     * @param object the object
     * @return the reflect
     */
    public static @NotNull Reflect of(final @NotNull Object object) {
        if (object instanceof Class) return of((Class<?>) object);
        else if (object instanceof String) return of((String) object);
        else return new Reflect(object.getClass(), object);
    }

    /**
     * Instantiates a new Reflect object.
     *
     * @param type the type
     * @return the reflect
     */
    public static @NotNull Reflect of(final @NotNull Class<?> type) {
        return new Reflect(type, type);
    }

    /**
     * Instantiates a new Reflect object.
     *
     * @param className the class name
     * @return the reflect
     * @throws ReflectException if it could not find the class
     */
    public static @NotNull Reflect of(final @NotNull String className) {
        try {
            return of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw ReflectException.classNotFound(className);
        }
    }

    /**
     * Instantiates a new Reflect object.
     *
     * @param className   the class name
     * @param classLoader the class loader to load the class from
     * @return the reflect
     * @throws ReflectException if it could not find the class
     */
    public static @NotNull Reflect of(final @NotNull String className, final @NotNull ClassLoader classLoader) {
        try {
            return of(classLoader.loadClass(className));
        } catch (ClassNotFoundException e) {
            throw ReflectException.classNotFound(className);
        }
    }

    /**
     * Converts a primitive type to its corresponding wrapper type.
     *
     * @param type the type
     * @return the wrapper type
     */
    public static @NotNull Class<?> toWrapper(final @NotNull Class<?> type) {
        return PRIMITIVE_TO_WRAPPER.getOrDefault(type, type);
    }

}
