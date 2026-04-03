package it.fulminazzo.blocksmith.function;

/**
 * A function that takes three parameters but does not return anything.
 *
 * @param <F> the type of the first parameter
 * @param <S> the type of the second parameter
 * @param <T> the type of the third parameter
 */
@FunctionalInterface
public interface TriConsumer<F, S, T> {

    /**
     * Calls the function with the given parameters.
     *
     * @param first  the first parameter
     * @param second the second parameter
     * @param third  the third parameter
     */
    void accept(final F first, final S second, final T third);

}
