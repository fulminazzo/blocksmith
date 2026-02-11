package it.fulminazzo.blocksmith.function;

/**
 * A re-creation of {@link java.util.function.Supplier} supporting checked exceptions.
 *
 * @param <T> the type of the return
 * @param <X> the type of the exception
 */
public interface SupplierException<T, X extends Exception> {

    /**
     * Gets a new object from this function.
     *
     * @return the result of the function
     * @throws X in case of any errors
     */
    T get() throws X;

}
