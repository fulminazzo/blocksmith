package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.argument.dto.Coordinate;
import it.fulminazzo.blocksmith.command.argument.dto.Position;
import it.fulminazzo.blocksmith.command.argument.dto.WorldPosition;
import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Holds all the supported argument parsers.
 */
@SuppressWarnings("unchecked")
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
            public @NotNull Boolean parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                String rawArgument = visitor.getInput().getCurrent();
                if (rawArgument.equalsIgnoreCase(TRUE)) return true;
                else if (rawArgument.equalsIgnoreCase(FALSE)) return false;
                else
                    throw new ArgumentParseException(CommandMessages.INVALID_BOOLEAN)
                            .arguments(Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, rawArgument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                return booleanCompletions;
            }

        });
        register(Character.class, new ArgumentParser<>() {

            @Override
            public @NotNull Character parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                String rawArgument = visitor.getInput().getCurrent();
                if (rawArgument.length() == 1) return rawArgument.charAt(0);
                else
                    throw new ArgumentParseException(CommandMessages.INVALID_CHARACTER)
                            .arguments(Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, rawArgument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                if (visitor.getInput().getCurrent().isEmpty()) return charactersCompletions;
                return Collections.emptyList();
            }

        });
        register(String.class, new ArgumentParser<>() {

            @Override
            public @NotNull String parse(final @NotNull InputVisitor<?, ?> visitor) {
                return visitor.getInput().getCurrent();
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                return Collections.singletonList("<%name%>");
            }

        });
        register(Object.class, new ArgumentParser<>() {

            @Override
            public @NotNull Object parse(final @NotNull InputVisitor<?, ?> visitor) {
                return visitor.getInput().getCurrent();
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                return Collections.singletonList("<%name%>");
            }

        });
        register(Locale.class, new ArgumentParser<>() {

            @Override
            public @NotNull Locale parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                String argument = visitor.getInput().getCurrent();
                Locale locale = LocaleUtils.fromString(argument);
                if (ArgumentParsers.isValidLocale(locale)) return locale;
                else throw new ArgumentParseException(CommandMessages.INVALID_LOCALE)
                        .arguments(Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                return localeCompletions;
            }

        });
        register(Coordinate.class, new ArgumentParser<>() {
            private final @NotNull ArgumentParser<Double> valueParser = of(Double.class);

            @Override
            public @NotNull Coordinate parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                final CommandInput input = visitor.getInput();
                String rawArgument = input.getCurrent();
                final String relativeIdentifier = Coordinate.RELATIVE_IDENTIFIER;
                boolean isRelative = rawArgument.startsWith(relativeIdentifier);
                if (isRelative) {
                    rawArgument = rawArgument.substring(relativeIdentifier.length());
                    if (rawArgument.isEmpty()) rawArgument = "0";
                    input.setCurrent(rawArgument);
                }
                return new Coordinate(Objects.requireNonNull(valueParser.parse(visitor)), isRelative);
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                final CommandInput input = visitor.getInput();
                final String current = input.getCurrent();
                final String relativeIdentifier = Coordinate.RELATIVE_IDENTIFIER;
                if (current.isEmpty()) return Collections.singletonList(relativeIdentifier);
                if (current.startsWith(relativeIdentifier)) {
                    input.setCurrent(current.substring(relativeIdentifier.length()));
                    List<String> completions = valueParser.getCompletions(visitor);
                    input.setCurrent(current);
                    return completions.isEmpty()
                            ? completions
                            : completions.stream()
                              .map(c -> relativeIdentifier + c)
                              .collect(Collectors.toList());
                } else return valueParser.getCompletions(visitor);
            }

        });
        register(Position.class, new MultiArgumentParser<>(
                l -> new Position((Coordinate) l.get(0), (Coordinate) l.get(1), (Coordinate) l.get(2)),
                Coordinate.class, Coordinate.class, Coordinate.class
        ));
        register(WorldPosition.class, new MultiArgumentParser<>(
                l -> {
                    Position position = (Position) l.get(1);
                    return new WorldPosition((String) l.get(0), position.getX(), position.getY(), position.getZ());
                },
                new ArgumentParser<>() {

                    @Override
                    public @NotNull String parse(final @NotNull InputVisitor<?, ?> visitor) {
                        return visitor.getInput().getCurrent();
                    }

                    @Override
                    public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                        return List.of("<world>");
                    }

                }, of(Position.class)
        ));
    }

    /**
     * Gets the most appropriate argument parser for the given type.
     *
     * @param <T>  the type of the argument
     * @param type the Java class of the argument
     * @return the argument parser
     * @throws IllegalArgumentException if no parser is found
     */
    @SuppressWarnings({"rawtypes"})
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
     * Retrieves the Java type associated with the given argument parser.
     *
     * @param parser the argument parser
     * @param <T>    the type of the argument
     * @return the Java class of the argument
     * @throws IllegalArgumentException if the parser is not registered yet
     */
    public static <T> @NotNull Class<T> type(final @NotNull ArgumentParser<T> parser) {
        return (Class<T>) parsers.entrySet().stream()
                .filter(e -> e.getValue().equals(parser))
                .map(Map.Entry::getKey)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(ReflectException.formatMessage(
                        "Argument parser has not been registered yet. Please use %s#register",
                        ArgumentParsers.class
                )));
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