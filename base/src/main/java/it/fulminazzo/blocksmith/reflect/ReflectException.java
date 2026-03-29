package it.fulminazzo.blocksmith.reflect;

import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown by {@link Reflect} on errors.
 */
public final class ReflectException extends RuntimeException {
    
    private ReflectException(final @NotNull String format, final Object @NotNull ... args) {
        super(String.format(format, args));
    }
    
    /**
     * Instantiates a new Reflect exception.
     *
     * @param cause  the cause that generated the exception
     * @param format the format
     * @param args   the args
     */
    ReflectException(final @NotNull Throwable cause, final @NotNull String format, final Object @NotNull ... args) {
        super(String.format(format, args), cause);
    }

    /**
     * Cannot find field reflect exception.
     *
     * @param type      the type
     * @param fieldName the field name
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotFindField(final @NotNull Class<?> type,
                                                     final @NotNull String fieldName) {
        return new ReflectException("Could not find field '%s' in class '%s'", fieldName, type.getCanonicalName());
    }

    /**
     * Cannot find field reflect exception.
     *
     * @param type the type
     * @return the reflect exception
     */
    static @NotNull ReflectException cannotFindField(final @NotNull Class<?> type) {
        return new ReflectException("Could not find field with the given predicate in class '%s'", type.getCanonicalName());
    }
    
}
