package it.fulminazzo.blocksmith.function;

/**
 * A function that takes one parameter but does not return anything.
 * Might throw the specified exception during execution.
 *
 * @param <T> the type of the parameter
 * @param <X> the type of the exception thrown by this function
 */
@FunctionalInterface
public interface ConsumerException<T, X extends Exception> {

    /**
     * Calls the function with the given parameters.
     *
     * @param parameter the parameter
     * @throws X in case of an error during execution
     */
    void accept(final T parameter) throws X;

}
