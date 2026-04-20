package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds all the supported argument parsers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgumentParsers {
    private static final @NotNull Map<@NotNull Class<?>, @NotNull ArgumentParser<?>> parsers = new ConcurrentHashMap<>();

    /*
     * COMPLETIONS
     */

    private static final @NotNull String TRUE = Boolean.TRUE.toString();
    private static final @NotNull String FALSE = Boolean.FALSE.toString();
    private static final @NotNull List<String> booleanCompletions = Arrays.asList(TRUE, FALSE);

    private static final @NotNull List<String> charactersCompletions;

    private static final @NotNull List<String> localeCompletions;

    static {
        charactersCompletions = new ArrayList<>();
        for (char c = 'a'; c <= 'z'; c++) charactersCompletions.add(String.valueOf(c));
        for (char c = 'A'; c <= 'Z'; c++) charactersCompletions.add(String.valueOf(c));
        for (char c = '0'; c <= '9'; c++) charactersCompletions.add(String.valueOf(c));

        localeCompletions = new ArrayList<>();
        Arrays.stream(Locale.getAvailableLocales())
                .filter(ArgumentParsers::isValidLocale)
                .map(LocaleUtils::toString)
                .distinct()
                .forEach(localeCompletions::add);

        register(Byte.class, new NumberArgumentParser<>(Byte.MIN_VALUE, Byte.MAX_VALUE, Byte::valueOf));
        register(Short.class, new NumberArgumentParser<>(Short.MIN_VALUE, Short.MAX_VALUE, Short::valueOf));
        register(Integer.class, new NumberArgumentParser<>(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::valueOf));
        register(Long.class, new NumberArgumentParser<>(Long.MIN_VALUE, Long.MAX_VALUE, Long::valueOf));
        register(Float.class, new NumberArgumentParser<>(-Float.MAX_VALUE, Float.MAX_VALUE, Float::valueOf));
        register(Double.class, new NumberArgumentParser<>(-Double.MAX_VALUE, Double.MAX_VALUE, Double::valueOf));
        register(Boolean.class, new ArgumentParser<>() {

            @Override
            public @NotNull Boolean parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
                String rawArgument = visitor.getInput().getCurrent();
                if (rawArgument.equalsIgnoreCase(TRUE)) return true;
                else if (rawArgument.equalsIgnoreCase(FALSE)) return false;
                else
                    throw new ArgumentParseException(CommandMessages.INVALID_BOOLEAN)
                            .arguments(Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, rawArgument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
                return booleanCompletions;
            }

        });
        register(Character.class, new ArgumentParser<>() {

            @Override
            public @NotNull Character parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
                String rawArgument = visitor.getInput().getCurrent();
                if (rawArgument.length() == 1) return rawArgument.charAt(0);
                else
                    throw new ArgumentParseException(CommandMessages.INVALID_CHARACTER)
                            .arguments(Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, rawArgument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
                if (visitor.getInput().getCurrent().isEmpty()) return charactersCompletions;
                return Collections.emptyList();
            }

        });
        register(String.class, new ArgumentParser<>() {

            @Override
            public @NotNull String parse(final @NotNull Visitor<?, ?> visitor) {
                return visitor.getInput().getCurrent();
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
                return Collections.singletonList("<%name%>");
            }

        });
        register(Locale.class, new ArgumentParser<>() {

            @Override
            public @NotNull Locale parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
                String argument = visitor.getInput().getCurrent();
                Locale locale = LocaleUtils.fromString(argument);
                if (ArgumentParsers.isValidLocale(locale)) return locale;
                else throw new ArgumentParseException(CommandMessages.INVALID_LOCALE)
                        .arguments(Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
                return localeCompletions;
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
        if (parser != null) return (ArgumentParser<T>) parser;
        else throw new IllegalArgumentException(ReflectException.formatMessage(
                "No default Argument parser supports the type %s. Please provide a custom parser through %s#register",
                type, ArgumentParsers.class
        ));
    }

    /**
     * Registers a new argument parser.
     *
     * @param <T>    the type of the argument
     * @param type   the java class of the argument
     * @param parser the argument parser
     */
    public static <T> void register(final @NotNull Class<T> type, final @NotNull ArgumentParser<T> parser) {
        parsers.put(type, parser);
    }

    private static boolean isValidLocale(final @NotNull Locale locale) {
        return !locale.getLanguage().isEmpty() && !locale.getCountry().isEmpty();
    }

}