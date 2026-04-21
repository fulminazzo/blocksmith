package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.visitor.Visitor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * A special {@link ArgumentParser} that delegates the parsing to another parser.
 *
 * @param <F> the type of the delegate
 * @param <T> the type of the Java object
 */
public final class DelegateArgumentParser<F, T> implements ArgumentParser<T> {
    @Getter
    private final @NotNull ArgumentParser<F> delegate;
    private final @NotNull BiFunction<@NotNull Visitor<?, ?>, @Nullable F, @Nullable T> converter;

    /**
     * Instantiates a new Delegate argument parser.
     *
     * @param converter    the function to convert the parsed object to the desired type
     * @param delegateType the Java type of the delegate argument
     */
    public DelegateArgumentParser(final @NotNull BiFunction<@NotNull Visitor<?, ?>, @Nullable F, @Nullable T> converter,
                                  final @NotNull Class<F> delegateType) {
        this(converter, ArgumentParsers.of(delegateType));
    }

    /**
     * Instantiates a new Delegate argument parser.
     *
     * @param converter the function to convert the parsed object to the desired type
     * @param delegate  the parser delegated of parsing
     */
    public DelegateArgumentParser(final @NotNull BiFunction<@NotNull Visitor<?, ?>, @Nullable F, @Nullable T> converter,
                                  final @NotNull ArgumentParser<F> delegate) {
        this.delegate = delegate;
        this.converter = converter;
    }

    @Override
    public boolean tryAdvanceCursor(final @NotNull Visitor<?, ?> visitor) {
        return delegate.tryAdvanceCursor(visitor);
    }

    @Override
    public @Nullable T parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
        return converter.apply(visitor, delegate.parse(visitor));
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        return delegate.getCompletions(visitor);
    }

}
