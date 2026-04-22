package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A special {@link ArgumentParser} supporting multiple arguments to instantiate one single type.
 * <br>
 * An example of usage is for Minecraft block positions.
 * These objects are defined from three coordinates (x, y and z).
 * With default parsers, the developer should either request three {@link Double}s in their command method
 * or request a string argument that will be parsed (for example {@code <x>:<y>:<z>}
 * or quoted {@code "<x> <y> <z>"}).
 * <br>
 * An alternative could be to create a new {@link MultiArgumentParser} for the position Java type,
 * and require three {@link Double}s.
 * The parser will handle automatically conversion, completions and missing arguments,
 * resulting in the same format the user would find in case of a method with three {@link Double}s.
 *
 * @param <T> the type of the Java object
 */
public class MultiArgumentParser<T> implements ArgumentParser<T>, Iterable<ArgumentParser<?>> {
    protected final @NotNull Function<@NotNull List<Object>, @Nullable T> constructor;
    protected final @NotNull List<@NotNull ArgumentParser<?>> parsers;

    /**
     * Instantiates a new Multi argument parser.
     *
     * @param constructor   the function to create a new instance of the object.
     *                      The list will contain the parsed arguments
     *                      (and its size will be defined by the given argument types).
     *                      No check on the types is required (assuming the correct ones
     *                      were given in the argument types).
     * @param argumentTypes the Java type of the arguments
     */
    public MultiArgumentParser(final @NotNull Function<@NotNull List<Object>, @Nullable T> constructor,
                               final @NotNull Class<?> @NotNull ... argumentTypes) {
        this(constructor, Stream.of(argumentTypes).map(ArgumentParsers::of).toArray(ArgumentParser[]::new));
    }

    /**
     * Instantiates a new Multi argument parser.
     *
     * @param constructor     the function to create a new instance of the object.
     *                        The list will contain the parsed arguments
     *                        (and its size will be defined by the given argument types).
     *                        No check on the types is required (assuming the correct ones
     *                        were given in the argument types).
     * @param argumentParsers the parsers for the Java types
     */
    public MultiArgumentParser(final @NotNull Function<@NotNull List<Object>, @Nullable T> constructor,
                               final @NotNull ArgumentParser<?> @NotNull ... argumentParsers) {
        this.constructor = constructor;
        if (argumentParsers.length == 0)
            throw new IllegalArgumentException(String.format(
                    "Could not create %s: at least one argument type must be given",
                    getClass().getSimpleName()
            ));
        this.parsers = Arrays.asList(argumentParsers);
    }

    @Override
    public boolean tryAdvanceCursor(final @NotNull InputVisitor<?, ?> visitor) {
        final CommandInput input = visitor.getInput();
        CommandInput snapshot = input.snapshot();
        for (ArgumentParser<?> parser : parsers)
            if (input.isDone() || !parser.tryAdvanceCursor(visitor)) {
                input.restore(snapshot);
                return false;
            }
        return true;
    }

    @Override
    public @Nullable T parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
        final List<Object> parsed = new ArrayList<>();
        final CommandInput input = visitor.getInput();
        for (int i = 0; i < parsers.size(); i++) {
            if (input.isDone()) throw new ArgumentParseException(CommandMessages.NOT_ENOUGH_ARGUMENTS);
            ArgumentParser<?> parser = parsers.get(i);
            parsed.add(parser.parse(visitor));
            if (i != parsers.size() - 1) input.advanceCursor();
        }
        return constructor.apply(parsed);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
        final CommandInput input = visitor.getInput();
        for (int i = 0; i < parsers.size(); i++) {
            ArgumentParser<?> parser = parsers.get(i);
            if (input.isLast() || parser instanceof MultiArgumentParser<?>)
                return parser.getCompletions(visitor);
            try {
                parser.parse(visitor);
            } catch (ArgumentParseException e) {
                break;
            }
            if (i != parsers.size() - 1) input.advanceCursor();
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull Iterator<ArgumentParser<?>> iterator() {
        return parsers.iterator();
    }

}
