package it.fulminazzo.blocksmith.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

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
        return new ReflectException("Could not find field with the given predicate in type '%s'", type);
    }

    private static @NotNull String formatMessage(final @NotNull String format,
                                                 final Object @NotNull ... args) {
        for (int i = 0; i < args.length; i++) {
            Object object = args[i];
            if (object instanceof Type) args[i] = ReflectUtils.toString((Type) object);
        }
        return String.format(format, args);
    }

}
