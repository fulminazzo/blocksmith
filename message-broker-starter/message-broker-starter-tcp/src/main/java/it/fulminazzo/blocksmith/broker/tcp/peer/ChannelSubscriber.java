package it.fulminazzo.blocksmith.broker.tcp.peer;

import org.jetbrains.annotations.NotNull;

/**
 * Identifies an object capable of subscribing to channels.
 *
 * @param <C> the type of the channel subscriber (for method chaining)
 */
public interface ChannelSubscriber<C extends ChannelSubscriber<C>> {

    /**
     * Sends a message to the channel.
     * <br>
     * The message is <b>not</b> guaranteed to be delivered
     * (if the channel is offline at the time of writing).
     *
     * @param message the message to send
     */
    void send(final @NotNull String message);

    /**
     * Subscribes this object to a new channel.
     *
     * @param channel the channel name
     * @return this object (for method chaining)
     */
    @NotNull C subscribe(final @NotNull String channel);

    /**
     * Unsubscribes this object from a channel.
     *
     * @param channel the channel name
     * @return this object (for method chaining)
     */
    @NotNull C unsubscribe(final @NotNull String channel);

    /**
     * Checks if this object is subscribed to a channel.
     *
     * @param channel the channel name
     * @return {@code true} if the object is subscribed, {@code false} otherwise
     */
    boolean isSubscribed(final @NotNull String channel);

}
