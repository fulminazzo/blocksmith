package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * Identifies an object capable of publishing to a plugin messaging channel.
 * <br>
 * Must provide a recovery method in case of failed sending.
 *
 * @see AbstractPluginMessagePublisher
 * @see PluginMessageChannelCoordinator
 * @see ProxyPluginMessageChannelCoordinator
 */
public interface PluginMessagePublisher {

    /**
     * Attempts to publish a message to the channel.
     *
     * @param channelName the channel
     * @param message     the message
     * @return {@code true} if the message was successfully published;
     *         {@code false} if the message could not be sent at this time
     *         (probably due to missing bridge between connections)
     */
    boolean publish(final @NotNull String channelName, final byte @NotNull [] message);

    /**
     * Attempts to publish once again (in order) the messages whose {@link #publish(String, byte[])}
     * returned {@code false}.
     * <br>
     * <b>WARNING</b>: it is <b>not guaranteed</b> that the publication will be successful.
     */
    void republishFailedMessages();

}
