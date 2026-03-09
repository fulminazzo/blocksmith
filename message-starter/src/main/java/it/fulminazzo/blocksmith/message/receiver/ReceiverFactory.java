package it.fulminazzo.blocksmith.message.receiver;

import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link Receiver} objects accordingly.
 */
public interface ReceiverFactory {

    /**
     * Creates a new Receiver from the given one.
     *
     * @param <R>      the type of the receiver
     * @param receiver the receiver
     * @return the blocksmith receiver
     */
    <R> @NotNull Receiver<?> create(final @NotNull R receiver);

    /**
     * Checks if the current factory supports the given receiver type.
     *
     * @param receiverType the receiver type
     * @return <code>true</code> if it does
     */
    boolean supports(final @NotNull Class<?> receiverType);

}
