package it.fulminazzo.blocksmith.conversion;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for all the converters.
 * Check {@link Convertible} for more.
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ConversionRegistry {
    private static final @NotNull Map<Class<?>, ConverterRegistry<?>> CONVERTERS = new ConcurrentHashMap<>();

    /**
     * Gets the conversion function from the given type to the given target type.
     *
     * @param <T>    the type to convert
     * @param <R>    the type to convert to
     * @param type   the type to convert from
     * @param target the target type
     * @return the conversion function
     */
    public static <T, R> @NotNull Optional<Converter<T, R>> getConverter(final @NotNull Class<T> type,
                                                                         final @NotNull Class<R> target) {
        return getConverter(type).getConverter(target);
    }

    /**
     * Registers a new conversion function from the given type to the given target type.
     *
     * @param <T>       the type to convert
     * @param <R>       the type to convert to
     * @param type      the type to convert from
     * @param target    the target type
     * @param converter the conversion function
     */
    public static <T, R> void register(final @NotNull Class<T> type,
                                       final @NotNull Class<R> target,
                                       final @NotNull Converter<T, R> converter) {
        getConverter(type).register(target, converter);
    }

    private static <T> @NotNull ConverterRegistry<T> getConverter(final @NotNull Class<T> type) {
        return (ConverterRegistry<T>) CONVERTERS.computeIfAbsent(type, k -> new ConverterRegistry<>());
    }

    /**
     * Holds the converters for a given type.
     *
     * @param <T> the type the converters refer to
     */
    @Value
    public static class ConverterRegistry<T> {
        @NotNull Map<Class<?>, Converter<T, ?>> converters = new ConcurrentHashMap<>();

        /**
         * Gets the converter for the given target type.
         *
         * @param <R>    the target type
         * @param target the target type
         * @return the function to convert the given type to the target type
         */
        public <R> @NotNull Optional<Converter<T, R>> getConverter(final @NotNull Class<R> target) {
            return Optional.ofNullable((Converter<T, R>) converters.get(target));
        }

        /**
         * Register a converter for the given target type.
         *
         * @param <R>       the type of the target
         * @param target    the target type
         * @param converter the function to convert the given type to the target type
         */
        public <R> void register(final @NotNull Class<R> target, final @NotNull Converter<T, R> converter) {
            converters.put(target, converter);
        }

    }

}
