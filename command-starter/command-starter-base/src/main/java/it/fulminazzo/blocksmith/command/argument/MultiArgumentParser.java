package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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
public class MultiArgumentParser<T> implements ArgumentParser<T> {
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
        this.constructor = constructor;
        if (argumentTypes.length == 0)
            throw new IllegalArgumentException(String.format(
                    "Could not create %s: at least one argument type must be given",
                    getClass().getSimpleName()
            ));
        this.parsers = new ArrayList<>();
        for (final Class<?> type : argumentTypes)
            parsers.add(ArgumentParsers.of(type));
    }

    @Override
    public @Nullable T parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
        final List<Object> parsed = new ArrayList<>();
        final CommandInput input = visitor.getInput();
        for (int i = 0; i < parsers.size(); i++) {
            if (input.isDone()) throw new ArgumentParseException("error.not-enough-arguments");
            ArgumentParser<?> parser = parsers.get(i);
            parsed.add(parser.parse(visitor));
            if (i != parsers.size() - 1) input.advanceCursor();
        }
        return constructor.apply(parsed);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        final CommandInput input = visitor.getInput();
        for (int i = 0; i < parsers.size(); i++) {
            ArgumentParser<?> parser = parsers.get(i);
            if (input.isLast()) return parser.getCompletions(visitor);
            try {
                parser.parse(visitor);
            } catch (ArgumentParseException e) {
                break;
            }
            if (i != parsers.size() - 1) input.advanceCursor();
        }
        return Collections.emptyList();
    }

}
