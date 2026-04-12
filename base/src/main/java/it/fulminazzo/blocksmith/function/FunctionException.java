package it.fulminazzo.blocksmith.function;

/**
 * A function that takes one parameter and returns a result.
 * Might throw the specified exception during execution.
 *
 * @param <T> the type of the argument
 * @param <R> the type of the result
 * @param <X> the type of the exception thrown by this function
 */
@FunctionalInterface
public interface FunctionException<T, R, X extends Exception> {

    /**
     * Calls the function with the given parameters.
     *
     * @param parameter the parameter
     * @return the result
     * @throws X in case of an error during execution
     */
    R apply(final T parameter) throws X;

}
