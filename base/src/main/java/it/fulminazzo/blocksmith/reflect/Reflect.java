package it.fulminazzo.blocksmith.reflect;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A wrapper for Java objects to work with reflections.
 */
@SuppressWarnings("unchecked")
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Reflect {
    private static final @NotNull Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class,
            boolean.class, Boolean.class
    );
    private static final @NotNull Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = PRIMITIVE_TO_WRAPPER.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    private static final @NotNull Map<Class<?>, Function<Number, Object>> NUMBERS_CONVERTER = new HashMap<>();

    static {
        NUMBERS_CONVERTER.put(byte.class, Number::byteValue);
        NUMBERS_CONVERTER.put(Byte.class, Number::byteValue);
        NUMBERS_CONVERTER.put(char.class, n -> (char) n.intValue());
        NUMBERS_CONVERTER.put(Character.class, n -> (char) n.intValue());
        NUMBERS_CONVERTER.put(short.class, Number::shortValue);
        NUMBERS_CONVERTER.put(Short.class, Number::shortValue);
        NUMBERS_CONVERTER.put(int.class, Number::intValue);
        NUMBERS_CONVERTER.put(Integer.class, Number::intValue);
        NUMBERS_CONVERTER.put(long.class, Number::longValue);
        NUMBERS_CONVERTER.put(Long.class, Number::longValue);
        NUMBERS_CONVERTER.put(float.class, Number::floatValue);
        NUMBERS_CONVERTER.put(Float.class, Number::floatValue);
        NUMBERS_CONVERTER.put(double.class, Number::doubleValue);
        NUMBERS_CONVERTER.put(Double.class, Number::doubleValue);
    }

    @NotNull Type type;
    @Getter(AccessLevel.NONE)
    Object object;

    /**
     * Gets the internal wrapped object.
     *
     * @param <T> the type of the object
     * @return the object
     */
    public <T> T get() {
        return (T) object;
    }

    /**
     * Gets the internal wrapped object Java class.
     *
     * @return the class
     */
    public @NotNull Class<?> getObjectClass() {
        return ReflectUtils.toClass(type);
    }

    /*
     * TYPE
     */

    /**
     * Checks if the type is primitive.
     *
     * @return <code>true</code> if it is
     */
    public boolean isPrimitive() {
        return PRIMITIVE_TO_WRAPPER.containsKey(getObjectClass());
    }

    /**
     * Checks if the type is a Java wrapper type.
     *
     * @return <code>true</code> if it is
     */
    public boolean isWrapper() {
        return PRIMITIVE_TO_WRAPPER.containsValue(getObjectClass());
    }

    /**
     * Checks if the type is a base Java type.
     * A base type is a primitive, wrapper or {@link String}.
     *
     * @return <code>true</code> if it is
     */
    public boolean isBaseType() {
        return isPrimitive() || isWrapper() || getObjectClass().equals(String.class);
    }

    /**
     * Checks if the class currently stored extends the given type.
     * <br>
     * <b>WARNING</b>: will not check for generic types correctness
     *
     * @param type the type to check
     * @return <code>true</code> if it does
     */
    public boolean extendsType(final @NotNull Type type) {
        return ReflectUtils.extendsType(getObjectClass(), type);
    }

    /**
     * Converts the internal type to a Java wrapper type (if the type is primitive).
     *
     * @return the reflect with the new object
     */
    public @NotNull Reflect toWrapper() {
        if (isPrimitive()) {
            Class<?> newType = PRIMITIVE_TO_WRAPPER.get(getObjectClass());
            final Object newObject;
            if (object instanceof Class<?>) newObject = newType;
            else newObject = cast(newType, object);
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
            Class<?> newType = WRAPPER_TO_PRIMITIVE.get(getObjectClass());
            final Object newObject;
            if (object instanceof Class<?>) newObject = newType;
            else newObject = cast(newType, object);
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
        return getFieldObject(getInstanceField(name));
    }

    /**
     * Gets the value of the static field with the given name.
     *
     * @param name the name of the field
     * @return the value of the field
     * @throws ReflectException if no field is found or an error occurs while getting the value
     */
    public @NotNull Reflect getStaticFieldObject(final @NotNull String name) {
        return getFieldObject(getStaticField(name));
    }

    /**
     * Gets the value of the field with the given name.
     *
     * @param name the name of the field
     * @return the value of the field
     * @throws ReflectException if no field is found or an error occurs while getting the value
     */
    public @NotNull Reflect getFieldObject(final @NotNull String name) {
        return getFieldObject(getField(name));
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
    public @NotNull Reflect getFieldObject(final @NotNull Field field) {
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
        try {
            return getField(f -> !Modifier.isStatic(f.getModifiers()) && f.getName().equals(name));
        } catch (ReflectException e) {
            throw ReflectException.cannotFindField(type, name);
        }
    }

    /**
     * Gets the static field with the given name.
     *
     * @param name the name of the field
     * @return the field
     * @throws ReflectException if no field is found
     */
    public @NotNull Field getStaticField(final @NotNull String name) {
        try {
            return getField(f -> Modifier.isStatic(f.getModifiers()) && f.getName().equals(name));
        } catch (ReflectException e) {
            throw ReflectException.cannotFindField(type, name);
        }
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
        Class<?> type = getObjectClass();
        while (type != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }

    @Override
    public @NotNull String toString() {
        String objectDeclaration;
        if (object == null) objectDeclaration = "null";
        else objectDeclaration = String.format("%s (type: %s)", object, object.getClass().getCanonicalName());
        return String.format("%s(type=%s, object=%s)", getClass().getCanonicalName(), ReflectUtils.toString(type), objectDeclaration);
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
    public static @NotNull Reflect of(final Object object) {
        if (object instanceof Type) return of((Type) object);
        else if (object instanceof String) return of((String) object);
        else return new Reflect(object.getClass(), object);
    }

    /**
     * Instantiates a new Reflect object.
     *
     * @param type the type
     * @return the reflect
     */
    public static @NotNull Reflect of(final @NotNull Type type) {
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

    /*
     * UTILITIES
     */

    /**
     * Converts a primitive type to its corresponding wrapper type.
     *
     * @param type the type
     * @return the wrapper type
     */
    public static @NotNull Class<?> toWrapper(final @NotNull Class<?> type) {
        return PRIMITIVE_TO_WRAPPER.getOrDefault(type, type);
    }

    /**
     * Casts the Java type to the given object.
     * Useful for dynamic casting of wrapper and primitive types.
     *
     * @param <T>    the type of the class to cast
     * @param type   the class
     * @param object the object to cast
     * @return the cast object
     * @throws ReflectException if the cast fails
     */
    public static <T> T cast(final @NotNull Class<T> type, final Object object) {
        if (object instanceof Number) {
            Function<Number, Object> converter = NUMBERS_CONVERTER.get(type);
            if (converter != null) return (T) converter.apply((Number) object);
        } else if (object instanceof Character) {
            Function<Number, Object> converter = NUMBERS_CONVERTER.get(type);
            if (converter != null) return (T) converter.apply((int) (Character) object);
        }
        if (object instanceof Boolean && type.equals(boolean.class)) return (T) object;
        try {
            if (type.isPrimitive() && object == null) throw new ClassCastException();
            return type.cast(object);
        } catch (ClassCastException e) {
            throw ReflectException.cannotCast(object, type);
        }
    }

}
