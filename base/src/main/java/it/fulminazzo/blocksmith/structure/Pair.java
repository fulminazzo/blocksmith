package it.fulminazzo.blocksmith.structure;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A wrapper for two objects.
 *
 * @param <F> the type of the first object
 * @param <S> the type of the second object
 */
@Value(staticConstructor = "of")
public class Pair<F, S> implements Serializable, Comparable<Pair<F, S>> {
    F first;
    S second;

    /**
     * Applies the given function to the contained objects.
     *
     * @param <T>    the type of the result
     * @param mapper the function to apply
     * @return the result
     */
    public <T> T map(final @NotNull BiFunction<F, S, T> mapper) {
        return mapper.apply(first, second);
    }

    /**
     * Instantiates a new pair with the first object mapped through the function.
     *
     * @param <T>    the type of the new first object
     * @param mapper the function to apply
     * @return the new pair
     */
    public <T> @NotNull Pair<T, S> mapFirst(final @NotNull Function<F, T> mapper) {
        return new Pair<>(mapper.apply(first), second);
    }

    /**
     * Instantiates a new pair with the second object mapped through the function.
     *
     * @param <T>    the type of the new second object
     * @param mapper the function to apply
     * @return the new pair
     */
    public <T> @NotNull Pair<F, T> mapSecond(final @NotNull Function<S, T> mapper) {
        return new Pair<>(first, mapper.apply(second));
    }

    /**
     * Instantiates a new pair with first and second objects inverted.
     *
     * @return the new pair
     */
    public @NotNull Pair<S, F> swap() {
        return new Pair<>(second, first);
    }

    @Override
    public int compareTo(final @NotNull Pair<F, S> other) {
        int c = compare(first, other.first);
        if (c != 0) return c;
        else return compare(second, other.second);
    }

    @SuppressWarnings("unchecked")
    private static <T> int compare(final T first, final T second) {
        if (first == null && second == null) return 0;
        else if (first == null) return 1;
        else if (second == null) return -1;
        if (first instanceof Comparable)
            return ((Comparable<T>) first).compareTo(second);
        else throw new IllegalArgumentException(String.format("Cannot compare non-%s type %s",
                Comparable.class.getSimpleName(), first.getClass().getCanonicalName()
        ));
    }

}
