//TODO: update
//package it.fulminazzo.blocksmith.command;
//
//import com.mojang.brigadier.arguments.ArgumentType;
//import com.mojang.brigadier.arguments.BoolArgumentType;
//import com.mojang.brigadier.arguments.StringArgumentType;
//import it.fulminazzo.blocksmith.util.ReflectionUtils;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Contains custom {@link ArgumentType} associated with Java types.
// */
//public final class ArgumentTypes {
//    private static final @NotNull Map<Class<?>, ArgumentType<?>> converters = new ConcurrentHashMap<>();
//
//    static {
//        register(Boolean.class, BoolArgumentType.bool());
//        register(String.class, StringArgumentType.string());
//    }
//
//    /**
//     * Registers a new custom argument type.
//     *
//     * @param type      the Java class
//     * @param argumentType the argument type
//     */
//    public static void register(final @NotNull Class<?> type,
//                                final @NotNull ArgumentType<?> argumentType) {
//        converters.put(type, argumentType);
//    }
//
//    /**
//     * Gets the argument type associated with the given type.
//     *
//     * @param <T>  the type of the parser
//     * @param type the Java class
//     * @return the argument type (if found)
//     */
//    @SuppressWarnings("unchecked")
//    public static <T> @NotNull Optional<ArgumentType<T>> get(final @NotNull Class<?> type) {
//        return Optional.ofNullable((ArgumentType<T>) converters.get(ReflectionUtils.toWrapper(type)));
//    }
//
//}
