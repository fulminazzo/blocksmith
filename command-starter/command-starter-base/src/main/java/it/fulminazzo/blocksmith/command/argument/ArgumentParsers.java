package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all the supported argument parsers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgumentParsers {
    private static final @NotNull Map<Class<?>, ArgumentParser<?>> PARSERS = new HashMap<>();

    static {
        register(String.class, s -> s);
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
        ArgumentParser<?> parser = PARSERS.get(ReflectionUtils.toWrapper(type));
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
        PARSERS.put(type, parser);
    }

}