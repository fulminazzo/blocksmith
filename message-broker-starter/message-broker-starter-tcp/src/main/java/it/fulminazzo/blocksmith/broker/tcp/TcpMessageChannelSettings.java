package it.fulminazzo.blocksmith.broker.tcp;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * Message channel settings for TCP connections.
 *
 * @see TcpMessageChannel
 * @see TcpMessageBroker
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class TcpMessageChannelSettings extends MessageChannelSettings {

    @Override
    public @NotNull TcpMessageChannelSettings withChannelName(final @NotNull String channelName) {
        return (TcpMessageChannelSettings) super.withChannelName(channelName);
    }

    @Override
    public @NotNull TcpMessageChannelSettings broadcast() {
        return (TcpMessageChannelSettings) super.broadcast();
    }

    @Override
    public @NotNull TcpMessageChannelSettings direct(final @NotNull String subchannelName) {
        return (TcpMessageChannelSettings) super.direct(subchannelName);
    }

}
