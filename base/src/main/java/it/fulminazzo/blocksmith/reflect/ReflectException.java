package it.fulminazzo.blocksmith.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An exception thrown by {@link Reflect} on errors.
 */
public final class ReflectException extends RuntimeException {

    /**
     * Instantiates a new Reflect exception.
     *
     * @param format the format of the message
     * @param args   the arguments to format
     */
    ReflectException(final @NotNull String format, final Object @NotNull ... args) {
        super(formatMessage(format, args));
    }

    /**
     * Instantiates a new Reflect exception.
     *
     * @param cause  the cause that generated the exception
     * @param format the format of the message
     * @param args   the arguments to format
     */
    ReflectException(final @NotNull Throwable cause, final @NotNull String format, final Object @NotNull ... args) {
        super(formatMessage(format, args), cause);
    }

    /**
     * Class not found reflect exception.
     *
     * @param className the class name
     * @return the reflect exception
     */
    static @NotNull ReflectException classNotFound(final @NotNull String className) {
        return new ReflectException("Could not find class '%s'", className);
    }

    /**
     * Cannot cast reflect exception.
     *
     * @param object the object
     * @param type   the type
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotCast(final Object object, final Type type) {
        final String objectDeclaration;
        if (object == null) objectDeclaration = "null";
        else objectDeclaration = object + " (type: " + object.getClass().getCanonicalName() + ")";
        return new ReflectException("Cannot cast object '%s' to type '%s'", objectDeclaration, type);
    }

    /**
     * Cannot find constructor reflect exception.
     *
     * @param type           the type
     * @param parameterTypes the parameter types
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotFindConstructor(final @NotNull Type type,
                                                           final @Nullable Class<?> @NotNull ... parameterTypes) {
        return new ReflectException("Could not find constructor with types (%s) in type '%s'",
                Arrays.stream(parameterTypes)
                        .map(p -> p == null ? "?" : ReflectUtils.toString(p))
                        .collect(Collectors.joining(", ")),
                type
        );
    }

    /**
     * Cannot find constructor reflect exception.
     *
     * @param type the type
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotFindConstructor(final @NotNull Type type) {
        return new ReflectException("Could not find constructor from the given predicate in type '%s'", type);
    }

    /**
     * Cannot find field reflect exception.
     *
     * @param type      the type
     * @param fieldName the field name
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotFindField(final @NotNull Type type,
                                                     final @NotNull String fieldName) {
        return new ReflectException("Could not find field '%s' in type '%s'", fieldName, type);
    }

    /**
     * Cannot find field reflect exception.
     *
     * @param type the type
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotFindField(final @NotNull Type type) {
        return new ReflectException("Could not find field from the given predicate in type '%s'", type);
    }

    /**
     * Cannot find method reflect exception.
     *
     * @param type           the type
     * @param returnType     the return type
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotFindMethod(final @NotNull Type type,
                                                      final @Nullable Type returnType,
                                                      final @Nullable String methodName,
                                                      final @Nullable Class<?> @NotNull ... parameterTypes) {
        return new ReflectException("Could not find method %s %s(%s) in type '%s'",
                returnType == null ? "?" : ReflectUtils.toString(returnType),
                methodName == null ? "?" : methodName,
                Arrays.stream(parameterTypes)
                        .map(p -> p == null ? "?" : ReflectUtils.toString(p))
                        .collect(Collectors.joining(", ")),
                type
        );
    }

    /**
     * Cannot find method reflect exception.
     *
     * @param type the type
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotFindMethod(final @NotNull Type type) {
        return new ReflectException("Could not find method from the given predicate in type '%s'", type);
    }

    /**
     * Formats the given message accordingly.
     * <br>
     * <ul>
     *     <li>If an argument is a {@link Type},
     *     it will be converted with {@link ReflectUtils#toString(Type)};</li>
     *     <li>If an argument is a {@link Method},
     *     it will be formatted as <code>&lt;method_name&gt;(&lt;method_parameters&gt;)</code>;</li>
     *     <li>If an argument is a {@link Constructor},
     *     it will be formatted as <code>&lt;declaring_class&gt;(&lt;method_parameters&gt;)</code>.</li>
     * </ul>
     *
     * @param format the format of the message
     * @param args   the arguments
     * @return the message
     */
    public static @NotNull String formatMessage(final @NotNull String format,
                                                final @Nullable Object @NotNull ... args) {
        for (int i = 0; i < args.length; i++) {
            Object object = args[i];
            if (object instanceof Type) args[i] = ReflectUtils.toString((Type) object);
            else if (object instanceof Method) {
                Method method = (Method) object;
                args[i] = String.format("%s %s(%s)",
                        ReflectUtils.toString(method.getReturnType()),
                        method.getName(),
                        Arrays.stream(method.getParameterTypes())
                                .map(ReflectUtils::toString)
                                .collect(Collectors.joining(", "))
                );
            } else if (object instanceof Constructor<?>) {
                Constructor<?> constructor = (Constructor<?>) object;
                args[i] = String.format("%s(%s)",
                        constructor.getDeclaringClass().getCanonicalName(),
                        Arrays.stream(constructor.getParameterTypes())
                                .map(ReflectUtils::toString)
                                .collect(Collectors.joining(", "))
                );
            }
        }
        return String.format(format, args);
    }

}
