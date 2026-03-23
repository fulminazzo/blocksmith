package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all the supported argument parsers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgumentParsers {
    private static final @NotNull Map<Class<?>, ArgumentParser<?>> parsers = new HashMap<>();

    static {
        register(Byte.class, new NumberArgumentParser<>(Byte.MIN_VALUE, Byte.MAX_VALUE, Byte::valueOf));
        register(Short.class, new NumberArgumentParser<>(Short.MIN_VALUE, Short.MAX_VALUE, Short::valueOf));
        register(Integer.class, new NumberArgumentParser<>(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::valueOf));
        register(Long.class, new NumberArgumentParser<>(Long.MIN_VALUE, Long.MAX_VALUE, Long::valueOf));
        register(Float.class, new NumberArgumentParser<>(Float.MIN_VALUE, Float.MAX_VALUE, Float::valueOf));
        register(Double.class, new NumberArgumentParser<>(Double.MIN_VALUE, Double.MAX_VALUE, Double::valueOf));
        register(String.class, new ArgumentParser<>() {

            @Override
            public @NotNull String parse(final @NotNull String rawArgument) {
                return rawArgument;
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                return Collections.singletonList("<%name%>");
            }

        });
    }

    /**
     * Gets the most appropriate argument parser for the given type.
     * Throws {@link IllegalArgumentException} if not found.
     *
     * @param <T>  the type of the argument
     * @param type the java class of the argument
     * @return the argument parser
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull ArgumentParser<T> of(final @NotNull Class<T> type) {
        ArgumentParser<?> parser = parsers.get(ReflectionUtils.toWrapper(type));
        if (parser == null)
            throw new IllegalArgumentException("No parser found for type " + type.getCanonicalName());
        return (ArgumentParser<T>) parser;
    }

    /**
     * Registers a new argument parser.
     *
     * @param <T>    the type of the argument
     * @param type   the java class of the argument
     * @param parser the argument parser
     */
    public static <T> void register(final @NotNull Class<T> type,
                                    final @NotNull ArgumentParser<T> parser) {
        parsers.put(type, parser);
    }

}