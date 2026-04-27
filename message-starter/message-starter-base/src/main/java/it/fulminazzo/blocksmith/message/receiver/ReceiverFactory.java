package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.ServerApplication;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Creates {@link Receiver} objects accordingly.
 */
public interface ReceiverFactory {

    /**
     * Sets up the factory internal fields.
     * <br>
     * <b>WARNING</b>: this is required for all the other methods to work properly.
     *
     * @param application the application using the factory
     * @return this object (for method chaining)
     */
    @NotNull ReceiverFactory setup(final @NotNull ServerApplication application);

    /**
     * Gets all the receivers of the platform.
     * In Minecraft servers it will include all the players and the console.
     *
     * @return the receivers
     */
    @NotNull Collection<Receiver> getAllReceivers();

    /**
     * Creates a new Receiver from the given one.
     *
     * @param <R>      the type of the receiver
     * @param receiver the receiver
     * @return the blocksmith receiver
     */
    <R> @NotNull Receiver create(final @NotNull R receiver);

    /**
     * Checks if the current factory supports the given receiver type.
     *
     * @param receiverType the receiver type
     * @return {@code true} if it does
     */
    boolean supports(final @NotNull Class<?> receiverType);

}
