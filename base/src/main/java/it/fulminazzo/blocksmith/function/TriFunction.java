package it.fulminazzo.blocksmith.function;

/**
 * A function that takes three parameters and returns a result.
 *
 * @param <F> the type of the first parameter
 * @param <S> the type of the second parameter
 * @param <T> the type of the third parameter
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface TriFunction<F, S, T, R> {

    /**
     * Calls the function with the given parameters.
     *
     * @param first  the first parameter
     * @param second the second parameter
     * @param third  the third parameter
     * @return the result
     */
    R apply(final F first, final S second, final T third);

}
