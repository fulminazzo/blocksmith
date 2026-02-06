package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A collection of utilities to work with reflections.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    /**
     * Initializes a new instance of the given class
     * through the constructor with matching parameters.
     *
     * @param <T>             the type of the object
     * @param type            the type
     * @param parametersTypes the parameters classes
     * @param parameters      the actual parameters
     * @return the object
     */
    public static <T> T initialize(final @NotNull Class<T> type,
                                   final @NotNull Collection<Class<?>> parametersTypes,
                                   final Object... parameters) {
        Class<?>[] actualParametersTypes = parametersTypes.toArray(new Class[0]);
        String parameterTypesNames = parametersTypes.stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(", "));
        try {
            Constructor<T> constructor = type.getDeclaredConstructor(actualParametersTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(parameters);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else throw new RuntimeException(cause);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("Could not find constructor %s(%s)",
                    type.getCanonicalName(), parameterTypesNames);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReflectionException(e, "Could not initialize %s(%s): %s",
                    type.getCanonicalName(), parameterTypesNames, e.getMessage());
        }
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

    /**
     * Attempts to find a field in the given class (or superclasses)
     * with the given name and arguments types.
     * <br>
     * The returned field can either be static or non-static.
     *
     * @param container      the container of the method
     * @param fieldName     the field name
     * @return the method
     */
    public static @NotNull Field getField(final @NotNull Class<?> container,
                                          final @NotNull String fieldName) {
        Class<?> curr = container;
        while (curr != null && !curr.equals(Object.class)) {
            try {
                return curr.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                curr = curr.getSuperclass();
            }
        }
        throw new ReflectionException("Could not get field '%s' from '%s': no such method was found",
                fieldName, container.getCanonicalName());
    }

    /**
     * Wrapper for:
     * <pre>
     * field.setAccessible(true);
     * field.get(caller);
     * </pre>
     * In case of error throws the unchecked {@link ReflectionException}.
     *
     * @param caller the caller
     * @param field  the field to get
     * @param <T>    the type of the field value
     * @return the value of the field
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(final @NotNull Object caller,
                                      final @NotNull Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(caller);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e, "Could not get value of %s.%s: %s",
                    caller.getClass().getCanonicalName(), field.getName(), e.getMessage());
        }
    }

    /**
     * Attempts to find a method in the given class (or superclasses)
     * with the given name and arguments types.
     * <br>
     * The returned method can either be static or non-static.
     *
     * @param container      the container of the method
     * @param methodName     the method name
     * @param argumentsTypes the types of the arguments
     * @return the method
     */
    public static @NotNull Method getMethod(final @NotNull Class<?> container,
                                            final @NotNull String methodName,
                                            final @NotNull Collection<Class<?>> argumentsTypes) {
        Class<?> curr = container;
        while (curr != null && !curr.equals(Object.class)) {
            try {
                return curr.getDeclaredMethod(methodName, argumentsTypes.toArray(new Class[0]));
            } catch (NoSuchMethodException ignored) {
                curr = curr.getSuperclass();
            }
        }
        throw new ReflectionException("Could not invoke method '%s' from '%s': no such method was found",
                methodName, container.getCanonicalName());
    }

    /**
     * Invokes a method (regardless if it is static or not) from the given object.
     *
     * @param <T>            the return type
     * @param caller         the object to call the method from
     * @param methodName     the method name
     * @param argumentsTypes the types of the arguments
     * @param arguments      the arguments
     * @return the returned object
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(final @NotNull Object caller,
                                     final @NotNull String methodName,
                                     final @NotNull Collection<Class<?>> argumentsTypes,
                                     final Object... arguments) {
        Class<?> clazz = caller instanceof Class ? (Class<?>) caller : caller.getClass();
        try {
            Method function = getMethod(clazz, methodName, argumentsTypes);
            function.setAccessible(true);
            return (T) function.invoke(caller, arguments);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else throw new RuntimeException(cause);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e, "Could not invoke method '%s' from '%s': %s",
                    methodName, clazz.getCanonicalName(), e.getMessage()
            );
        }
    }

    /**
     * An exception thrown by {@link ReflectionUtils} failed operations.
     */
    static final class ReflectionException extends RuntimeException {

        /**
         * Instantiates a new Reflection exception.
         *
         * @param format    the format of the message
         * @param arguments the arguments to parse in the message
         */
        public ReflectionException(final @NotNull String format,
                                   final @NotNull Object... arguments) {
            super(String.format(format, arguments));
        }

        /**
         * Instantiates a new Reflection exception.
         *
         * @param cause     the cause
         * @param format    the format of the message
         * @param arguments the arguments to parse in the message
         */
        public ReflectionException(final @NotNull Throwable cause,
                                   final @NotNull String format,
                                   final @NotNull Object... arguments) {
            super(String.format(format, arguments), cause);
        }

    }

}
