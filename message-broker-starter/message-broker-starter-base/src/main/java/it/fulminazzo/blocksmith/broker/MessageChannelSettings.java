package it.fulminazzo.blocksmith.broker;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a general data holder for message channel settings.
 * Implementation may vary according to message channel type.
 *
 * @param <S> the type of the settings
 */
@SuppressWarnings("unchecked")
@EqualsAndHashCode
@ToString
public abstract class MessageChannelSettings<S extends MessageChannelSettings<S>> {
    private @Nullable String channelName;

    private @Nullable MessageChannelType channelType;
    private @Nullable String subchannelName;

    public @NotNull String getChannelName() {
        return Objects.requireNonNull(channelName, "channel name has not been specified yet");
    }

    public @NotNull MessageChannelType getChannelType() {
        return Objects.requireNonNull(channelType, "channel type has not been specified yet");
    }

    public @NotNull String getSubchannelName() {
        return Objects.requireNonNull(
                subchannelName,
                "required subchannel name to be specified when using " + MessageChannelType.DIRECT
        );
    }

    public @NotNull S withChannelName(final @NotNull String channelName) {
        this.channelName = channelName;
        return (S) this;
    }

    public @NotNull S broadcast() {
        this.channelType = MessageChannelType.BROADCAST;
        return (S) this;
    }

    public @NotNull S direct(final @NotNull String subchannelName) {
        this.channelType = MessageChannelType.DIRECT;
        this.subchannelName = subchannelName;
        return (S) this;
    }

}
