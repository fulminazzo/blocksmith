package it.fulminazzo.blocksmith.function;

/**
 * A function that takes no parameters and returns a result.
 * Might throw the specified exception during execution.
 *
 * @param <R> the type of the result
 * @param <X> the type of the exception thrown by this function
 */
@FunctionalInterface
public interface SupplierException<R, X extends Exception> {

    /**
     * Calls the function.
     *
     * @return the result
     * @throws X in case of an error during execution
     */
    R get() throws X;

}
