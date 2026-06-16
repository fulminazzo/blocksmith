package it.fulminazzo.blocksmith.broker.plugin;

import it.fulminazzo.blocksmith.broker.AbstractMessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Message channel settings for Plugin channels.
 *
 * @see PluginMessageChannel
 * @see PluginMessageBroker
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class PluginMessageChannelSettings extends AbstractMessageChannelSettings {

    @Override
    public @NonNull PluginMessageChannelSettings withChannelName(final @NotNull String channelName) {
        return (PluginMessageChannelSettings) super.withChannelName(channelName);
    }

    @Override
    public @NonNull PluginMessageChannelSettings broadcast() {
        return (PluginMessageChannelSettings) super.broadcast();
    }

    @Override
    public @NonNull PluginMessageChannelSettings direct(final @NotNull String subchannelName) {
        return (PluginMessageChannelSettings) super.direct(subchannelName);
    }

}
