package it.fulminazzo.blocksmith.broker.memory;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * Message channel settings for in-memory channels.
 *
 * @see MemoryMessageChannel
 * @see MemoryMessageBroker
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class MemoryMessageChannelSettings extends MessageChannelSettings {

    @Override
    public @NotNull MemoryMessageChannelSettings withChannelName(final @NotNull String channelName) {
        return (MemoryMessageChannelSettings) super.withChannelName(channelName);
    }

    @Override
    public @NotNull MemoryMessageChannelSettings broadcast() {
        return (MemoryMessageChannelSettings) super.broadcast();
    }

    @Override
    public @NotNull MemoryMessageChannelSettings direct(final @NotNull String subchannelName) {
        return (MemoryMessageChannelSettings) super.direct(subchannelName);
    }

}
