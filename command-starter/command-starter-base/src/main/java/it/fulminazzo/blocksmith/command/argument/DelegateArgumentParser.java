package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.visitor.Visitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * A special {@link ArgumentParser} that delegates the parsing to another parser.
 *
 * @param <F> the type of the delegate
 * @param <T> the type of the Java object
 */
public final class DelegateArgumentParser<F, T> implements ArgumentParser<T> {
    private final @NotNull ArgumentParser<F> delegate;
    private final @NotNull Function<@Nullable F, @Nullable T> converter;

    /**
     * Instantiates a new Delegate argument parser.
     *
     * @param delegateType the Java type of the delegate argument
     * @param converter    the function to convert the parsed object to the desired type
     */
    public DelegateArgumentParser(final @NotNull Class<F> delegateType,
                                  final @NotNull Function<@Nullable F, @Nullable T> converter) {
        this(ArgumentParsers.of(delegateType), converter);
    }

    /**
     * Instantiates a new Delegate argument parser.
     *
     * @param delegate  the parser delegated of parsing
     * @param converter the function to convert the parsed object to the desired type
     */
    public DelegateArgumentParser(final @NotNull ArgumentParser<F> delegate,
                                  final @NotNull Function<@Nullable F, @Nullable T> converter) {
        this.delegate = delegate;
        this.converter = converter;
    }

    @Override
    public @Nullable T parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
        return converter.apply(delegate.parse(visitor));
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        return delegate.getCompletions(visitor);
    }

}
