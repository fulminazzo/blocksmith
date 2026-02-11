package it.fulminazzo.blocksmith.function;

/**
 * A re-creation of {@link java.util.function.Function} supporting checked exceptions.
 *
 * @param <T> the type of the first parameter
 * @param <U> the type of the second parameter
 * @param <R> the type of the return
 * @param <X> the type of the exception
 */
public interface BiFunctionException<T, U, R, X extends Exception> {

    /**
     * Applies the current function to the given parameter.
     *
     * @param first  the first parameter
     * @param second the second parameter
     * @return the result of the function
     * @throws X in case of any errors
     */
    R apply(final T first, final U second) throws X;

}
