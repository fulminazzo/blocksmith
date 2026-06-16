package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a general data holder for message channel settings.
 * Implementation may vary according to the message channel type.
 *
 * @see MessageChannel
 * @see MessageBroker
 */
public abstract class MessageChannelSettings {

    /**
     * Sets the channel name.
     *
     * @param channelName the channel name
     * @return this object (for method chaining)
     */
    public abstract @NotNull MessageChannelSettings withChannelName(final @NotNull String channelName);

    /**
     * Sets the channel type to {@link MessageChannelType#BROADCAST}.
     *
     * @return this object (for method chaining)
     */
    public abstract @NotNull MessageChannelSettings broadcast();

    /**
     * Sets the channel type to {@link MessageChannelType#DIRECT}.
     *
     * @param subchannelName the name of the subchannel
     * @return this object (for method chaining)
     */
    public abstract @NotNull MessageChannelSettings direct(final @NotNull String subchannelName);

}
