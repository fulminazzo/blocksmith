package it.fulminazzo.blocksmith.function;

/**
 * A re-creation of {@link java.util.function.Function} supporting checked exceptions.
 *
 * @param <T> the type of the parameter
 * @param <R> the type of the return
 * @param <X> the type of the exception
 */
public interface FunctionException<T, R, X extends Exception> {

    /**
     * Applies the current function to the given parameter.
     *
     * @param parameter the parameter
     * @return the result of the function
     * @throws X in case of any errors
     */
    R apply(final T parameter) throws X;

}
