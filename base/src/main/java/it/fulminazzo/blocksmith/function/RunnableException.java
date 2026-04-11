package it.fulminazzo.blocksmith.function;

/**
 * A function that takes no parameters and does not return anything.
 * Might throw the specified exception during execution.
 *
 * @param <X> the type of the exception thrown by this function
 */
@FunctionalInterface
public interface RunnableException<X extends Exception> {

    /**
     * Calls the function.
     *
     * @throws X in case of an error during execution
     */
    void run() throws X;

}
