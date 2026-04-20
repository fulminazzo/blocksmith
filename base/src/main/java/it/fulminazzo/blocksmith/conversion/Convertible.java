package it.fulminazzo.blocksmith.conversion;

import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Identifies an object that can be automatically converted into another type.
 * By default, this acts as a facade for casting types.
 * However, it is possible to register custom converters through {@link #register(Class, Class, Converter)}.
 */
public interface Convertible {

    /**
     * Converts the object into the given type.
     *
     * @param <T>  the type to convert to
     * @param type the type to convert to
     * @return the converted object
     */
    @SuppressWarnings("unchecked")
    default <T> T as(final @NotNull Class<T> type, final @Nullable Object @NotNull ... args) {
        Converter<Convertible, T> converter = (Converter<Convertible, T>) ConversionRegistry.getConverter(getClass(), type)
                .orElse((t, a) -> Reflect.cast(type, t));
        return converter.convert(this, args);
    }

    /**
     * Registers a new converter from the given type to the given class.
     * Converters are <b>unique</b>, meaning registering a converter
     * for a given type will overwrite any previous converter.
     *
     * @param <T>       the type to convert
     * @param <R>       the type to convert to
     * @param from      the type to convert
     * @param to        the type to convert to
     * @param converter the conversion function
     */
    static <T, R> void register(final @NotNull Class<T> from,
                                final @NotNull Class<R> to,
                                final @NotNull Converter<T, R> converter) {
        ConversionRegistry.register(from, to, converter);
    }

}
