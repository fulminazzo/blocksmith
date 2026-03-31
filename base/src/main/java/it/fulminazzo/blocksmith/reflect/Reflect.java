package it.fulminazzo.blocksmith.reflect;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A wrapper for Java objects to work with reflections.
 */
@SuppressWarnings("unchecked")
@Value
@EqualsAndHashCode(doNotUseGetters = true)
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

    @Nullable Type type;
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
     * Gets the internal wrapped object type.
     *
     * @return the type
     */
    public @NotNull Type getType() {
        if (type == null) throw new ReflectException("Reflect '%s' has null type", this);
        return type;
    }

    /**
     * Gets the internal wrapped object Java class.
     *
     * @return the class
     */
    public @NotNull Class<?> getObjectClass() {
        return ReflectUtils.toClass(getType());
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
        return WRAPPER_TO_PRIMITIVE.containsKey(getObjectClass());
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
            return cast(newType);
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
            return cast(newType);
        }
        return this;
    }

    /**
     * Casts the internal object to the given type.
     *
     * @param type the type
     * @return the reflect with the new object
     */
    public @NotNull Reflect cast(final @NotNull Class<?> type) {
        if (object instanceof Class<?>) return new Reflect(type, type);
        else return new Reflect(type, cast(type, object));
    }

    /*
     * CONSTRUCTORS
     */

    /**
     * Initializes a new instance of the internal type.
     *
     * @param parameters the parameters
     * @return the initialized object
     * @throws ReflectException if no method was found or an error occurs while getting the value
     */
    public @NotNull Reflect init(final @Nullable Object @NotNull ... parameters) {
        Class<?>[] parameterTypes = getClasses(parameters);
        Constructor<?> constructor = getConstructor(parameterTypes);
        return init(constructor, parameters);
    }

    /**
     * Initializes a new instance of the internal type.
     *
     * @param constructor the constructor
     * @param parameters  the parameters
     * @return the initialized object
     * @throws ReflectException if an error occurs while getting the value
     */
    public @NotNull Reflect init(final @NotNull Constructor<?> constructor,
                                 final @Nullable Object @NotNull ... parameters) {
        try {
            constructor.setAccessible(true);
            return new Reflect(
                    constructor.getDeclaringClass(),
                    constructor.newInstance(ReflectUtils.regroup(constructor.getParameters(), parameters))
            );
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else if (cause instanceof Error) throw (Error) cause;
            else throw new ReflectException(e, "Could not get instance from %s", constructor);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReflectException(e, "Could not get instance from %s", constructor);
        }
    }

    /**
     * Gets the constructor with the given parameter types.
     *
     * @param parameterTypes the parameter types
     * @return the constructor
     * @throws ReflectException if no constructor was found
     */
    public @NotNull Constructor<?> getConstructor(final @Nullable Class<?> @NotNull ... parameterTypes) {
        return ReflectUtils.findExecutable(
                getConstructors(),
                parameterTypes
        ).orElseThrow(() -> ReflectException.cannotFindConstructor(getType(), parameterTypes));
    }

    /**
     * Gets the first constructor that matches the predicate.
     *
     * @param predicate the predicate
     * @return the constructor
     * @throws ReflectException if no constructor was found
     */
    public @NotNull Constructor<?> getConstructor(final @NotNull Predicate<Constructor<?>> predicate) {
        return getConstructors(predicate).stream().findFirst().orElseThrow(() -> ReflectException.cannotFindConstructor(getType()));
    }

    /**
     * Gets all the constructors that match the predicate.
     *
     * @param predicate the predicate
     * @return the constructors
     */
    public @NotNull List<Constructor<?>> getConstructors(final @NotNull Predicate<Constructor<?>> predicate) {
        return getConstructors().stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Gets all the constructors.
     *
     * @return the constructors
     */
    public @NotNull List<Constructor<?>> getConstructors() {
        return Arrays.asList(getObjectClass().getDeclaredConstructors());
    }

    /*
     * FIELDS
     */

    /**
     * Gets the value of all the instance fields (fields must not be static).
     *
     * @return the values of the fields
     */
    public @NotNull List<Reflect> getInstanceFieldValues() {
        return getFieldValues(f -> !Modifier.isStatic(f.getModifiers()));
    }

    /**
     * Gets the values of all the static fields.
     *
     * @return the values of the fields
     */
    public @NotNull List<Reflect> getStaticFieldValues() {
        return getFieldValues(f -> Modifier.isStatic(f.getModifiers()));
    }

    /**
     * Gets the values of all the fields.
     *
     * @return the values of the fields
     */
    public @NotNull List<Reflect> getFieldValues() {
        return getFieldValues(f -> true);
    }

    /**
     * Gets the values of all the fields that match the predicate.
     *
     * @param predicate the predicate
     * @return the values of the fields
     */
    public @NotNull List<Reflect> getFieldValues(final @NotNull Predicate<Field> predicate) {
        return getFields(predicate).stream().map(this::get).collect(Collectors.toList());
    }

    /**
     * Sets a new value to the instance field with the given name (field must not be static).
     *
     * @param name  the name of the field
     * @param value the value of the field
     * @return this object (for method chaining)
     * @throws ReflectException if no field was found or an error occurs while setting the value
     */
    public @NotNull Reflect setInstance(final @NotNull String name, final Object value) {
        return set(getInstanceField(name), value);
    }

    /**
     * Sets a new value to the static field with the given name.
     *
     * @param name  the name of the field
     * @param value the value of the field
     * @return this object (for method chaining)
     * @throws ReflectException if no field was found or an error occurs while setting the value
     */
    public @NotNull Reflect setStatic(final @NotNull String name, final Object value) {
        return set(getStaticField(name), value);
    }

    /**
     * Sets a new value to the first field that matches the predicate.
     *
     * @param predicate the predicate
     * @param value     the value of the field
     * @return this object (for method chaining)
     * @throws ReflectException if no field was found or an error occurs while setting the value
     */
    public @NotNull Reflect set(final @NotNull Predicate<Field> predicate, final Object value) {
        return set(getField(predicate), value);
    }

    /**
     * Sets a new value to the field with the given name.
     *
     * @param name  the name of the field
     * @param value the value of the field
     * @return this object (for method chaining)
     * @throws ReflectException if no field was found or an error occurs while setting the value
     */
    public @NotNull Reflect set(final @NotNull String name, final Object value) {
        return set(getField(name), value);
    }

    /**
     * Sets a new value to the given field.
     *
     * @param field the field
     * @param value the value of the field
     * @return this object (for method chaining)
     * @throws ReflectException if an error occurs while setting the value
     */
    public @NotNull Reflect set(final @NotNull Field field, final Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
            return this;
        } catch (IllegalAccessException e) {
            throw new ReflectException(e, "Could not set value of field '%s' from %s", field.getName(), object);
        }
    }

    /**
     * Gets the value of the instance field with the given name (field must not be static).
     *
     * @param name   the name of the field
     * @param orElse the value to return if the field was not found or there were problems while accessing it
     * @return the value of the field
     */
    public @NotNull Reflect getInstance(final @NotNull String name, final Object orElse) {
        try {
            return get(getInstanceField(name), orElse);
        } catch (ReflectException ignored) {
            return new Reflect(orElse == null ? null : orElse.getClass(), orElse);
        }
    }

    /**
     * Gets the value of the static field with the given name.
     *
     * @param name   the name of the field
     * @param orElse the value to return if the field was not found or there were problems while accessing it
     * @return the value of the field
     */
    public @NotNull Reflect getStatic(final @NotNull String name, final Object orElse) {
        try {
            return get(getStaticField(name), orElse);
        } catch (ReflectException ignored) {
            return new Reflect(orElse == null ? null : orElse.getClass(), orElse);
        }
    }

    /**
     * Gets the value of the field with the given name.
     *
     * @param name   the name of the field
     * @param orElse the value to return if the field was not found or there were problems while accessing it
     * @return the value of the field
     */
    public @NotNull Reflect get(final @NotNull String name, final Object orElse) {
        try {
            return get(getField(name), orElse);
        } catch (ReflectException ignored) {
            return new Reflect(orElse == null ? null : orElse.getClass(), orElse);
        }
    }

    /**
     * Gets the value of the first field that matches the predicate.
     *
     * @param predicate the predicate
     * @param orElse    the value to return if the field was not found or there were problems while accessing it
     * @return the value of the field
     */
    public @NotNull Reflect get(final @NotNull Predicate<Field> predicate, final Object orElse) {
        try {
            return get(getField(predicate), orElse);
        } catch (ReflectException ignored) {
            return new Reflect(orElse == null ? null : orElse.getClass(), orElse);
        }
    }

    /**
     * Gets the value of the given field.
     *
     * @param field  the field
     * @param orElse the value to return if the field was not found or there were problems while accessing it
     * @return the value of the field
     */
    public @NotNull Reflect get(final @NotNull Field field, final Object orElse) {
        try {
            return get(field);
        } catch (ReflectException ignored) {
            return new Reflect(field.getGenericType(), cast(field.getType(), orElse));
        }
    }

    /**
     * Gets the value of the instance field with the given name (field must not be static).
     *
     * @param name the name of the field
     * @return the value of the field
     * @throws ReflectException if no field was found or an error occurs while getting the value
     */
    public @NotNull Reflect getInstance(final @NotNull String name) {
        return get(getInstanceField(name));
    }

    /**
     * Gets the value of the static field with the given name.
     *
     * @param name the name of the field
     * @return the value of the field
     * @throws ReflectException if no field was found or an error occurs while getting the value
     */
    public @NotNull Reflect getStatic(final @NotNull String name) {
        return get(getStaticField(name));
    }

    /**
     * Gets the value of the field with the given name.
     *
     * @param name the name of the field
     * @return the value of the field
     * @throws ReflectException if no field was found or an error occurs while getting the value
     */
    public @NotNull Reflect get(final @NotNull String name) {
        return get(getField(name));
    }

    /**
     * Gets the value of the first field that matches the predicate.
     *
     * @param predicate the predicate
     * @return the value of the field
     * @throws ReflectException if no field was found or an error occurs while getting the value
     */
    public @NotNull Reflect get(final @NotNull Predicate<Field> predicate) {
        return get(getField(predicate));
    }

    /**
     * Gets the value of the given field.
     *
     * @param field the field
     * @return the value of the field
     * @throws ReflectException if an error occurs while getting the value
     */
    public @NotNull Reflect get(final @NotNull Field field) {
        try {
            field.setAccessible(true);
            return new Reflect(field.getGenericType(), field.get(object));
        } catch (IllegalAccessException e) {
            throw new ReflectException(e, "Could not get value of field '%s' from %s", field.getName(), object);
        }
    }

    /**
     * Gets the instance field with the given name (field must not be static).
     *
     * @param name the name of the field
     * @return the field
     * @throws ReflectException if no field was found
     */
    public @NotNull Field getInstanceField(final @NotNull String name) {
        try {
            return getField(f -> !Modifier.isStatic(f.getModifiers()) && f.getName().equals(name));
        } catch (ReflectException e) {
            throw ReflectException.cannotFindField(getType(), name);
        }
    }

    /**
     * Gets the static field with the given name.
     *
     * @param name the name of the field
     * @return the field
     * @throws ReflectException if no field was found
     */
    public @NotNull Field getStaticField(final @NotNull String name) {
        try {
            return getField(f -> Modifier.isStatic(f.getModifiers()) && f.getName().equals(name));
        } catch (ReflectException e) {
            throw ReflectException.cannotFindField(getType(), name);
        }
    }

    /**
     * Gets the field with the given name.
     *
     * @param name the name of the field
     * @return the field
     * @throws ReflectException if no field was found
     */
    public @NotNull Field getField(final @NotNull String name) {
        try {
            return getField(f -> f.getName().equals(name));
        } catch (ReflectException e) {
            throw ReflectException.cannotFindField(getType(), name);
        }
    }

    /**
     * Gets the first field that matches the predicate.
     *
     * @param predicate the predicate
     * @return the field
     * @throws ReflectException if no field was found
     */
    public @NotNull Field getField(final @NotNull Predicate<Field> predicate) {
        return getFields(predicate).stream().findFirst().orElseThrow(() -> ReflectException.cannotFindField(getType()));
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
            Arrays.stream(type.getInterfaces())
                    .map(Class::getDeclaredFields)
                    .flatMap(Arrays::stream)
                    .forEach(fields::add);
            type = type.getSuperclass();
        }
        return fields;
    }

    /*
     * METHODS
     */

    /**
     * Invokes the method with the given parameters.
     *
     * @param parameters the parameters
     * @return the returned value
     * @throws ReflectException if no method was found or an error occurs while getting the value
     */
    public @NotNull Reflect invoke(final @Nullable Object @NotNull ... parameters) {
        return invoke((String) null, parameters);
    }

    /**
     * Invokes the method with the given name and parameters.
     *
     * @param name       the name (if <code>null</code> any method found will be accepted)
     * @param parameters the parameters
     * @return the returned value
     * @throws ReflectException if no method was found or an error occurs while getting the value
     */
    public @NotNull Reflect invoke(final @Nullable String name,
                                   final @Nullable Object @NotNull ... parameters) {
        return invoke(null, name, parameters);
    }

    /**
     * Invokes the method with the given name, return type and parameters.
     *
     * @param returnType the return type (if <code>null</code> any method found will be accepted)
     * @param name       the name (if <code>null</code> any method found will be accepted)
     * @param parameters the parameters
     * @return the returned value
     * @throws ReflectException if no method was found or an error occurs while getting the value
     */
    public @NotNull Reflect invoke(final @Nullable Class<?> returnType,
                                   final @Nullable String name,
                                   final @Nullable Object @NotNull ... parameters) {
        Class<?>[] parameterTypes = getClasses(parameters);
        Method method = getMethod(returnType, name, parameterTypes);
        return invoke(method, parameters);
    }

    /**
     * Invokes the given method.
     *
     * @param method     the method
     * @param parameters the parameters
     * @return the returned value
     * @throws ReflectException if an error occurs while getting the value
     */
    public @NotNull Reflect invoke(final @NotNull Method method,
                                   final @Nullable Object @NotNull ... parameters) {
        try {
            method.setAccessible(true);
            return new Reflect(
                    method.getGenericReturnType(),
                    method.invoke(
                            object,
                            ReflectUtils.regroup(method.getParameters(), parameters)
                    )
            );
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else if (cause instanceof Error) throw (Error) cause;
            else throw new ReflectException(e, "Could not invoke method %s on %s", method, object);
        } catch (IllegalAccessException e) {
            throw new ReflectException(e, "Could not invoke method %s on %s", method, object);
        }
    }

    /**
     * Gets the instance method with the given parameter types (method must not be static).
     *
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getInstanceMethod(final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getInstanceMethod(null, parameterTypes);
    }

    /**
     * Gets the static method with the given parameter types.
     *
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the static method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getStaticMethod(final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getStaticMethod(null, parameterTypes);
    }

    /**
     * Gets the method with the given parameter types.
     *
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getMethod(final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getMethod(null, parameterTypes);
    }

    /**
     * Gets the instance method with the given name and parameter types (method must not be static).
     *
     * @param name           the name (if <code>null</code> any method found will be accepted)
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getInstanceMethod(final @Nullable String name,
                                             final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getInstanceMethod(null, name, parameterTypes);
    }

    /**
     * Gets the static method with the given name and parameter types.
     *
     * @param name           the name (if <code>null</code> any method found will be accepted)
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the static method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getStaticMethod(final @Nullable String name,
                                           final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getStaticMethod(null, name, parameterTypes);
    }

    /**
     * Gets the method with the given name and parameter types.
     *
     * @param name           the name (if <code>null</code> any method found will be accepted)
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getMethod(final @Nullable String name,
                                     final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getMethod(null, name, parameterTypes);
    }

    /**
     * Gets the instance method with the given name, return type and parameter types (method must not be static).
     *
     * @param returnType     the return type (if <code>null</code> any method found will be accepted)
     * @param name           the name (if <code>null</code> any method found will be accepted)
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getInstanceMethod(final @Nullable Class<?> returnType,
                                             final @Nullable String name,
                                             final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getMethodHelper(m -> !Modifier.isStatic(m.getModifiers()), returnType, name, parameterTypes);
    }

    /**
     * Gets the static method with the given name, return type and parameter types.
     *
     * @param returnType     the return type (if <code>null</code> any method found will be accepted)
     * @param name           the name (if <code>null</code> any method found will be accepted)
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the static method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getStaticMethod(final @Nullable Class<?> returnType,
                                           final @Nullable String name,
                                           final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getMethodHelper(m -> Modifier.isStatic(m.getModifiers()), returnType, name, parameterTypes);
    }

    /**
     * Gets the method with the given name, return type and parameter types.
     *
     * @param returnType     the return type (if <code>null</code> any method found will be accepted)
     * @param name           the name (if <code>null</code> any method found will be accepted)
     * @param parameterTypes the parameter types (if a type is <code>null</code>, it will be considered as wildcard)
     * @return the method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getMethod(final @Nullable Class<?> returnType,
                                     final @Nullable String name,
                                     final @Nullable Class<?> @NotNull ... parameterTypes) {
        return getMethodHelper(m -> true, returnType, name, parameterTypes);
    }

    private @NotNull Method getMethodHelper(final @NotNull Predicate<Method> validator,
                                            final @Nullable Class<?> returnType,
                                            final @Nullable String name,
                                            final @Nullable Class<?> @NotNull ... parameterTypes) {
        return ReflectUtils.findExecutable(
                getMethods(m ->
                        (name == null || m.getName().equalsIgnoreCase(name)) &&
                                (returnType == null || ReflectUtils.extendsType(m.getReturnType(), returnType)) &&
                                validator.test(m)
                ),
                parameterTypes
        ).orElseThrow(() -> ReflectException.cannotFindMethod(getType(), returnType, name, parameterTypes));
    }

    /**
     * Gets the first method that matches the predicate.
     *
     * @param predicate the predicate
     * @return the method
     * @throws ReflectException if no method was found
     */
    public @NotNull Method getMethod(final @NotNull Predicate<Method> predicate) {
        return getMethods(predicate).stream().findFirst().orElseThrow(() -> ReflectException.cannotFindMethod(getType()));
    }

    /**
     * Gets all the instance methods (methods not declared as static).
     *
     * @return the methods
     */
    public @NotNull List<Method> getInstanceMethods() {
        return getMethods(m -> !Modifier.isStatic(m.getModifiers()));
    }

    /**
     * Gets all the static methods.
     *
     * @return the static methods
     */
    public @NotNull List<Method> getStaticMethods() {
        return getMethods(m -> Modifier.isStatic(m.getModifiers()));
    }

    /**
     * Gets all the methods that match the predicate.
     *
     * @param predicate the predicate
     * @return the methods
     */
    public @NotNull List<Method> getMethods(final @NotNull Predicate<Method> predicate) {
        return getMethods().stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Gets all the methods.
     *
     * @return the methods
     */
    public @NotNull List<Method> getMethods() {
        List<Method> methods = new ArrayList<>();
        Class<?> type = getObjectClass();
        while (type != null) {
            List<Method> list = sortMethods(Arrays.stream(type.getDeclaredMethods())).collect(Collectors.toList());
            methods.addAll(list);
            sortMethods(Arrays.stream(type.getInterfaces())
                    .map(Class::getDeclaredMethods)
                    .flatMap(Arrays::stream))
                    .forEach(methods::add);
            type = type.getSuperclass();
        }
        methods.removeIf(Method::isSynthetic);
        return methods;
    }

    private static @NotNull Stream<Method> sortMethods(final @NotNull Stream<Method> methods) {
        return methods.sorted(Comparator.<Method, Boolean>comparing(m -> Modifier.isStatic(m.getModifiers()))
                .thenComparing(Method::getName)
                .thenComparing(Method::getParameterCount));
    }

    /*
     * ENUMS
     */

    /**
     * Gets the name of the internal wrapped object assuming it is an instance of {@link Enum}.
     *
     * @return the name
     * @throws ReflectException if the object is not an enum
     */
    public @NotNull String name() {
        return getEnum().name();
    }

    /**
     * Gets the associated ordinal of the internal wrapped object assuming it is an instance of {@link Enum}.
     *
     * @return the ordinal
     * @throws ReflectException if the object is not an enum
     */
    public int ordinal() {
        return getEnum().ordinal();
    }

    /**
     * Gets the internal wrapped object as an instance of {@link Enum}.
     *
     * @param <E> the type of the enum
     * @return the object
     * @throws ReflectException if the object is not an enum
     */
    public <E extends Enum<E>> E getEnum() {
        try {
            return (E) object;
        } catch (ClassCastException e) {
            throw new ReflectException("%s is not an enum", object);
        }
    }

    /**
     * Gets the enum corresponding to the given name.
     *
     * @param <E>  the type of the enum
     * @param name the name (case insensitive)
     * @return the enum (if found)
     * @throws ReflectException if the wrapped type is not an enum
     */
    public <E extends Enum<E>> @NotNull Optional<E> valueOf(final @NotNull String name) {
        checkEnum();
        return values().stream()
                .filter(e -> e.name().equalsIgnoreCase(name))
                .map(e -> (E) e)
                .findFirst();
    }

    /**
     * Gets the enum values of the internal wrapped enum.
     *
     * @param <E> the type of the enum
     * @return the enum values
     * @throws ReflectException if the type is not an enum
     */
    public <E extends Enum<E>> @NotNull Set<E> values() {
        checkEnum();
        E[] values = invoke("values").get();
        return new HashSet<>(Arrays.asList(values));
    }

    /**
     * Gets the internal wrapped type as a Java enum.
     *
     * @param <E> the type of the enum
     * @return the enum class
     * @throws ReflectException if the type is not an enum
     */
    public <E extends Enum<E>> @NotNull Class<E> getEnumClass() {
        checkEnum();
        return (Class<E>) getObjectClass();
    }

    private void checkEnum() {
        Class<?> type = getObjectClass();
        if (!Enum.class.isAssignableFrom(type))
            throw new ReflectException("Type '%s' is not an enum", type);
    }

    /*
     * GENERAL
     */

    @Override
    public @NotNull String toString() {
        String objectDeclaration;
        if (object == null) objectDeclaration = "null";
        else objectDeclaration = String.format("%s (type: %s)", object, ReflectUtils.toString(object.getClass()));
        return String.format("%s(type=%s, object=%s)", getClass().getCanonicalName(),
                type == null ? "null" : ReflectUtils.toString(type),
                objectDeclaration
        );
    }

    private static Class<?> @NotNull [] getClasses(final @Nullable Object @NotNull ... parameters) {
        return Arrays.stream(parameters)
                .map(p -> p == null ? null : p.getClass())
                .toArray(Class<?>[]::new);
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
    public static @NotNull Reflect on(final Object object) {
        return new Reflect(object.getClass(), object);
    }

    /**
     * Instantiates a new Reflect object.
     *
     * @param type the type
     * @return the reflect
     */
    public static @NotNull Reflect on(final @NotNull Type type) {
        return new Reflect(type, type);
    }

    /**
     * Instantiates a new Reflect object.
     *
     * @param className the class name
     * @return the reflect
     * @throws ReflectException if it could not find the class
     */
    public static @NotNull Reflect on(final @NotNull String className) {
        try {
            return on(Class.forName(className));
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
    public static @NotNull Reflect on(final @NotNull String className, final @NotNull ClassLoader classLoader) {
        try {
            return on(classLoader.loadClass(className));
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
