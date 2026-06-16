package it.fulminazzo.blocksmith.broker;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Abstract implementation of the {@link MessageChannelSettings} interface.
 * Provides common functionality for all settings.
 *
 * @see MessageChannelSettings
 * @see MessageChannel
 * @see MessageBroker
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public abstract class AbstractMessageChannelSettings extends MessageChannelSettings {
    private @Nullable String channelName;

    private @Nullable MessageChannelType channelType;
    private @Nullable String subchannelName;

    /**
     * Gets the channel name.
     *
     * @return the channel name
     */
    public @NotNull String getChannelName() {
        return Objects.requireNonNull(channelName, "channel name has not been specified yet");
    }

    /**
     * Gets the subchannel name.
     *
     * @return the subchannel name
     */
    public @NotNull String getSubchannelName() {
        return Objects.requireNonNull(
                getSubchannelNameOrNull(),
                "required subchannel name to be specified when using " + MessageChannelType.DIRECT
        );
    }

    /**
     * Gets the subchannel name.
     *
     * @return the subchannel name or {@code null} if the channel type is not {@link MessageChannelType#DIRECT}
     */
    public @Nullable String getSubchannelNameOrNull() {
        return subchannelName;
    }

    /**
     * Gets the channel type.
     *
     * @return the channel type
     */
    public @NotNull MessageChannelType getChannelType() {
        return Objects.requireNonNull(channelType, "channel type has not been specified yet");
    }

    @Override
    public @NotNull MessageChannelSettings withChannelName(final @NotNull String channelName) {
        this.channelName = channelName;
        return this;
    }

    @Override
    public @NotNull MessageChannelSettings broadcast() {
        this.channelType = MessageChannelType.BROADCAST;
        this.subchannelName = null;
        return this;
    }

    @Override
    public @NotNull MessageChannelSettings direct(final @NotNull String subchannelName) {
        this.channelType = MessageChannelType.DIRECT;
        this.subchannelName = subchannelName;
        return this;
    }

}
