package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
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
