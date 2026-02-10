package it.fulminazzo.blocksmith.function;

/**
 * A re-creation of {@link java.util.function.Consumer} supporting checked exceptions.
 *
 * @param <T> the type of the first parameter
 * @param <U> the type of the second parameter
 * @param <X> the type of the exception
 */
public interface BiConsumerException<T, U, X extends Exception> {

    /**
     * Applies the current function to the given parameter.
     *
     * @param first the first parameter
     * @param second the second parameter
     * @throws X in case of any errors
     */
    void accept(final T first, final U second) throws X;

}
