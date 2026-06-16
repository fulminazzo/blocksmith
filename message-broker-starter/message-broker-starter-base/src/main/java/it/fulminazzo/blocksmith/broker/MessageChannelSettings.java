package it.fulminazzo.blocksmith.broker;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a general data holder for message channel settings.
 * Implementation may vary according to the message channel type.
 *
 * @see MessageChannel
 * @see MessageBroker
 */
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public abstract class MessageChannelSettings {
    private @Nullable String channelName;

    private @Nullable MessageChannelType channelType;
    private @Nullable String subchannelName;

    /**
     * Sets the channel name.
     *
     * @param channelName the channel name
     * @return this object (for method chaining)
     */
    public @NotNull MessageChannelSettings withChannelName(final @NotNull String channelName) {
        this.channelName = channelName;
        return this;
    }

    /**
     * Sets the channel type to {@link MessageChannelType#BROADCAST}.
     *
     * @return this object (for method chaining)
     */
    public @NotNull MessageChannelSettings broadcast() {
        this.channelType = MessageChannelType.BROADCAST;
        this.subchannelName = null;
        return this;
    }

    /**
     * Sets the channel type to {@link MessageChannelType#DIRECT}.
     *
     * @param subchannelName the name of the subchannel
     * @return this object (for method chaining)
     */
    public @NotNull MessageChannelSettings direct(final @NotNull String subchannelName) {
        this.channelType = MessageChannelType.DIRECT;
        this.subchannelName = subchannelName;
        return this;
    }

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

}
