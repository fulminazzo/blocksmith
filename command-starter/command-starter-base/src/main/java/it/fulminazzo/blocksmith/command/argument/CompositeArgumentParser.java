package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.function.SupplierException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A special {@link ArgumentParser} that provides completions through multiple {@link ArgumentParser}s
 * and attempts to parse the input using the first successful parser.
 *
 * @param <T> the type of the Java object
 */
public final class CompositeArgumentParser<T> implements ArgumentParser<T> {
    private final @NotNull List<@NotNull ArgumentParser<? extends T>> parsers;

    /**
     * Instantiates a new Composite argument parser.
     *
     * @param argumentTypes the Java classes of the arguments
     */
    @SafeVarargs
    public CompositeArgumentParser(final @NotNull Class<? extends T> @NotNull ... argumentTypes) {
        this((Type[]) argumentTypes);
    }

    /**
     * Instantiates a new Composite argument parser.
     *
     * @param argumentTypes the Java type of the arguments
     */
    @SuppressWarnings("unchecked")
    public CompositeArgumentParser(final @NotNull Type @NotNull ... argumentTypes) {
        if (argumentTypes.length == 0)
            throw new IllegalArgumentException(String.format(
                    "Could not create %s: at least one argument type must be given",
                    getClass().getSimpleName()
            ));
        this.parsers = Arrays.stream(argumentTypes)
                .map(ArgumentParsers::of)
                .map(a -> (ArgumentParser<? extends T>) a)
                .collect(Collectors.toList());
    }

    /**
     * Instantiates a new Composite argument parser.
     *
     * @param argumentParsers the parsers for the Java types
     */
    @SafeVarargs
    public CompositeArgumentParser(final @NotNull ArgumentParser<? extends T> @NotNull ... argumentParsers) {
        if (argumentParsers.length == 0)
            throw new IllegalArgumentException(String.format(
                    "Could not create %s: at least one argument type must be given",
                    getClass().getSimpleName()
            ));
        this.parsers = new ArrayList<>(Arrays.asList(argumentParsers));
    }

    @Override
    public @Nullable T parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
        ArgumentParseException lastException = null;
        for (ArgumentParser<? extends T> parser : parsers)
            try {
                return snapshot(visitor, () -> parser.parse(visitor));
            } catch (ArgumentParseException e) {
                lastException = e;
            }
        throw Objects.requireNonNull(lastException);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
        Set<String> completions = new LinkedHashSet<>();
        for (ArgumentParser<? extends T> parser : parsers)
            completions.addAll(snapshot(visitor, () -> parser.getCompletions(visitor)));
        return new ArrayList<>(completions);
    }

    /**
     * Executes the given function, restoring the input to its previous state.
     *
     * @param <E>      the type of the result
     * @param <X>      the type of the exception to throw in case of errors during the execution of the function
     * @param visitor  the visitor requesting the completions
     * @param function the function to execute
     * @return the result of the function
     * @throws X in case of an error during the execution of the function
     */
    <E, X extends Exception> E snapshot(final @NotNull InputVisitor<?, ?> visitor,
                                        final @NotNull SupplierException<E, X> function) throws X {
        final CommandInput input = visitor.getInput();
        CommandInput snapshot = input.snapshot();
        E result = function.get();
        input.restore(snapshot);
        return result;
    }

}
