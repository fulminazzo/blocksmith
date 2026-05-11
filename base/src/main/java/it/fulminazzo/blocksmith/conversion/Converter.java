package it.fulminazzo.blocksmith.conversion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Functional interface to convert objects into other types.
 *
 * @param <T> the type to convert from
 * @param <R> the type to convert to
 */
@FunctionalInterface
public interface Converter<T, R> {

    /**
     * Converts the given object into another type.
     *
     * @param from the object to convert
     * @param args the arguments to pass to the converter
     * @return the converted type
     */
    R convert(final T from, final @Nullable Object @NotNull ... args);

}
