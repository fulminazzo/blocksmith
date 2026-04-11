package it.fulminazzo.blocksmith.function;

/**
 * A function that takes three parameters but does not return anything.
 * Might throw the specified exception during execution.
 *
 * @param <F> the type of the first parameter
 * @param <S> the type of the second parameter
 * @param <T> the type of the third parameter
 * @param <X> the type of the exception thrown by this function
 */
@FunctionalInterface
public interface TriConsumerException<F, S, T, X extends Exception> {

    /**
     * Calls the function with the given parameters.
     *
     * @param first  the first parameter
     * @param second the second parameter
     * @param third  the third parameter
     * @throws X in case of an error during execution
     */
    void accept(final F first, final S second, final T third) throws X;

}
