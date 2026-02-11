package it.fulminazzo.blocksmith.function;

/**
 * A re-creation of {@link java.util.function.Consumer} supporting checked exceptions.
 *
 * @param <T> the type of the parameter
 * @param <X> the type of the exception
 */
public interface ConsumerException<T, X extends Exception> {

    /**
     * Applies the current function to the given parameter.
     *
     * @param parameter the parameter
     * @throws X in case of any errors
     */
    void accept(final T parameter) throws X;

}
