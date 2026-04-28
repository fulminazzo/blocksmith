package it.fulminazzo.blocksmith.broker;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a general data holder for message channel settings.
 * Implementation may vary according to message channel type.
 */
@EqualsAndHashCode
@ToString
public abstract class MessageChannelSettings {
    private @Nullable String channelName;

    public @NotNull String getChannelName() {
        return Objects.requireNonNull(channelName, "channel name has not been specified yet");
    }

    public @NotNull MessageChannelSettings withChannelName(final @NotNull String channelName) {
        this.channelName = channelName;
        return this;
    }

}
