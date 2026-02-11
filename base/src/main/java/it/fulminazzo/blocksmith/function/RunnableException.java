package it.fulminazzo.blocksmith.function;

/**
 * A re-creation of {@link Runnable} supporting checked exceptions.
 *
 * @param <X> the type of the exception
 */
public interface RunnableException<X extends Exception> {

    /**
     * Runs the given function.
     *
     * @throws X in case of any errors
     */
    void run() throws X;

}
