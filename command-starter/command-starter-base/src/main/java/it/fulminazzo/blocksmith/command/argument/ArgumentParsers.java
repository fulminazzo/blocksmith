package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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
        register(Float.class, new NumberArgumentParser<>(-Float.MAX_VALUE, Float.MAX_VALUE, Float::valueOf));
        register(Double.class, new NumberArgumentParser<>(-Double.MAX_VALUE, Double.MAX_VALUE, Double::valueOf));
        register(Boolean.class, new ArgumentParser<>() {
            private final String TRUE = Boolean.TRUE.toString();
            private final String FALSE = Boolean.FALSE.toString();

            @Override
            public @NotNull Boolean parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                String rawArgument = context.getCurrent();
                if (rawArgument.equalsIgnoreCase(TRUE)) return true;
                else if (rawArgument.equalsIgnoreCase(FALSE)) return false;
                else
                    throw new CommandExecutionException("error.invalid-boolean").arguments(Placeholder.of("argument", rawArgument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                return Arrays.asList(TRUE, FALSE);
            }

        });
        register(Character.class, new ArgumentParser<>() {

            @Override
            public @NotNull Character parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                String rawArgument = context.getCurrent();
                if (rawArgument.length() == 1) return rawArgument.charAt(0);
                else
                    throw new CommandExecutionException("error.invalid-character").arguments(Placeholder.of("argument", rawArgument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                List<String> completions = new ArrayList<>();
                if (context.getCurrent().isEmpty()) {
                    for (char c = 'a'; c <= 'z'; c++) completions.add(String.valueOf(c));
                    for (char c = 'A'; c <= 'Z'; c++) completions.add(String.valueOf(c));
                    for (char c = '0'; c <= '9'; c++) completions.add(String.valueOf(c));
                }
                return completions;
            }

        });
        register(String.class, new ArgumentParser<>() {

            @Override
            public @NotNull String parse(final @NotNull CommandExecutionContext context) {
                return context.getCurrent();
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                return Collections.singletonList("<%name%>");
            }

        });
        register(Locale.class, new ArgumentParser<>() {
            private final @NotNull List<String> availableLocales = new ArrayList<>();

            @Override
            public @NotNull Locale parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                String argument = context.getCurrent();
                Locale locale = LocaleUtils.fromString(argument);
                if (isValid(locale)) return locale;
                else throw new CommandExecutionException("error.invalid-locale")
                        .arguments(Placeholder.of("argument", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                if (availableLocales.isEmpty())
                    availableLocales.addAll(Arrays.stream(Locale.getAvailableLocales())
                            .filter(this::isValid)
                            .map(LocaleUtils::toString)
                            .distinct()
                            .collect(Collectors.toList()));
                return availableLocales;
            }

            private boolean isValid(final @NotNull Locale locale) {
                return !locale.getLanguage().isEmpty() && !locale.getCountry().isEmpty();
            }

        });
    }

    /**
     * Gets the most appropriate argument parser for the given type.
     * Throws {@link IllegalArgumentException} if not found.
     *
     * @param <T>  the type of the argument
     * @param type the Java class of the argument
     * @return the argument parser
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> @NotNull ArgumentParser<T> of(final @NotNull Class<T> type) {
        if (Enum.class.isAssignableFrom(type))
            return (ArgumentParser<T>) parsers.computeIfAbsent(type, t -> new EnumArgumentParser<>((Class) t));
        ArgumentParser<?> parser = parsers.get(Reflect.toWrapper(type));
        if (parser == null)
            throw new IllegalArgumentException(String.format("No default Argument parser supports the type %s. " +
                    "Please provide a custom parser through %s#register", type.getCanonicalName(), ArgumentParsers.class.getSimpleName()));
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