package it.fulminazzo.blocksmith.function;

/**
 * A function that takes three parameters and returns a result.
 * Might throw the specified exception during execution.
 *
 * @param <F> the type of the first parameter
 * @param <S> the type of the second parameter
 * @param <T> the type of the third parameter
 * @param <R> the type of the result
 * @param <X> the type of the exception thrown by this function
 */
@FunctionalInterface
public interface TriFunctionException<F, S, T, R, X extends Exception> {

    /**
     * Calls the function with the given parameters.
     *
     * @param first  the first parameter
     * @param second the second parameter
     * @param third  the third parameter
     * @return the result
     * @throws X in case of an error during execution
     */
    R apply(final F first, final S second, final T third) throws X;

}
