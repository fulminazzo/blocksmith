package it.fulminazzo.blocksmith.broker.redis;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * Message channel settings for Redis channels.
 *
 * @see RedisMessageChannel
 * @see RedisMessageBroker
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class RedisMessageChannelSettings extends MessageChannelSettings {

    @Override
    public @NotNull RedisMessageChannelSettings withChannelName(final @NotNull String channelName) {
        return (RedisMessageChannelSettings) super.withChannelName(channelName);
    }

    @Override
    public @NotNull RedisMessageChannelSettings broadcast() {
        return (RedisMessageChannelSettings) super.broadcast();
    }

    @Override
    public @NotNull RedisMessageChannelSettings direct(final @NotNull String subchannelName) {
        return (RedisMessageChannelSettings) super.direct(subchannelName);
    }

}
