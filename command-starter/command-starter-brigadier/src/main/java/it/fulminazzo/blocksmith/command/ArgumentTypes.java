package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.arguments.*;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains custom {@link ArgumentType} associated with Java types.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgumentTypes {
    private static final @NotNull Map<Class<?>, ArgumentType<?>> converters = new ConcurrentHashMap<>();

    static {
        register(Byte.class, IntegerArgumentType.integer(Byte.MIN_VALUE, Byte.MAX_VALUE));
        register(Short.class, IntegerArgumentType.integer(Short.MIN_VALUE, Short.MAX_VALUE));
        register(Integer.class, IntegerArgumentType.integer());
        register(Long.class, LongArgumentType.longArg());
        register(Float.class, FloatArgumentType.floatArg());
        register(Double.class, DoubleArgumentType.doubleArg());
        register(Boolean.class, BoolArgumentType.bool());
        register(String.class, StringArgumentType.string());
    }

    /**
     * Registers a new custom argument type.
     *
     * @param type         the Java class
     * @param argumentType the argument type
     */
    public static void register(final @NotNull Class<?> type,
                                final @NotNull ArgumentType<?> argumentType) {
        converters.put(type, argumentType);
    }

    /**
     * Gets the argument type associated with the given type.
     *
     * @param <T>  the type of the parser
     * @param type the Java class
     * @return the argument type (if found)
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull Optional<ArgumentType<T>> get(final @NotNull Class<?> type) {
        return Optional.ofNullable((ArgumentType<T>) converters.get(Reflect.toWrapper(type)));
    }

}
