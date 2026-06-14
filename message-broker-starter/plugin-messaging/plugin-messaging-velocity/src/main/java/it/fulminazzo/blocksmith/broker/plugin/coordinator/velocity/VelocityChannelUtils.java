package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity;

import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A collection of utilities to work with Velocity channels.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VelocityChannelUtils {
    /**
     * The fallback namespace to use if the channel name does not contain one.
     */
    static final @NotNull String FALLBACK_NAMESPACE = UUID.randomUUID().toString();

    /**
     * Converts the raw channel name to a Velocity channel identifier.
     * <br>
     * If the channel contains a colon (":"), it will be split into two parts,
     * where the first part will be the namespace and the second part will be the channel name.
     * Otherwise, {@link #FALLBACK_NAMESPACE} will be used as namespace.
     *
     * @param channelName the channel name
     * @return the channel identifier
     */
    public static @NotNull ChannelIdentifier toIdentifier(final @NotNull String channelName) {
        final String namespace;
        final String channel;
        if (channelName.contains(":")) {
            int index = channelName.indexOf(':');
            namespace = channelName.substring(0, index);
            channel = channelName.substring(index + 1);
        } else {
            namespace = FALLBACK_NAMESPACE;
            channel = channelName;
        }
        return MinecraftChannelIdentifier.create(namespace, channel);
    }

    /**
     * Converts a Velocity channel identifier to a raw channel name.
     * <br>
     * If the namespace is {@link #FALLBACK_NAMESPACE}, the channel name will be returned as is.
     *
     * @param identifier the channel identifier
     * @return the channel name
     */
    public static @NotNull String toChannelName(final @NotNull ChannelIdentifier identifier) {
        String id = identifier.getId();
        if (id.startsWith(FALLBACK_NAMESPACE + ":"))
            id = id.substring(FALLBACK_NAMESPACE.length() + 1);
        return id;
    }

}
