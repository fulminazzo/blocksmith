package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main brain of the entire {@code plugin-messaging} module,
 * responsible for keeping track of opened channels, publications
 * and listening.
 *
 * <br>
 * <b>WARNING</b>: while using the classes of this module,
 * it is advised to <b>not interact directly</b> with any channel that
 * the module might use. It is better to leave the handling of specific
 * channels to the classes of this package to avoid any conflicts or
 * undesired behavior.
 *
 * @see PluginMessageHandler
 * @see PluginMessagePublisher
 * @see PluginMessageRegistrar
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PluginMessageChannelCoordinator implements PluginMessagePublisher {
    protected final @NotNull PluginMessageRegistrar registrar;

    private final @NotNull Map<String, List<PluginMessageHandler>> handlers = new ConcurrentHashMap<>();

    /**
     * Registers a new channel.
     *
     * @param channelName the channel name
     */
    protected abstract void registerChannel(final @NotNull String channelName);

    /**
     * Unregisters a channel.
     *
     * @param channelName the channel name
     */
    protected abstract void unregisterChannel(final @NotNull String channelName);

    /**
     * Registers a new handler for the specified channel.
     *
     * @param channelName the channel name
     * @param handler     the handler
     */
    public void registerHandler(final @NotNull String channelName, final @NotNull PluginMessageHandler handler) {
        handlers.computeIfAbsent(
                channelName,
                k -> {
                    registerChannel(k);
                    return new ArrayList<>();
                }
        ).add(handler);
    }

    /**
     * Unregisters the handler for all the channels it was registered for.
     * <br>
     * Unregisters channels if no other handler is registered.
     *
     * @param handler the handler
     */
    public void unregisterHandler(final @NotNull PluginMessageHandler handler) {
        for (Map.Entry<String, List<PluginMessageHandler>> entry : handlers.entrySet()) {
            String channelName = entry.getKey();
            List<PluginMessageHandler> channelHandlers = entry.getValue();
            channelHandlers.remove(handler);
            if (channelHandlers.isEmpty()) {
                handlers.remove(channelName);
                unregisterChannel(channelName);
            }
        }
    }

    /**
     * Handles an incoming message.
     *
     * @param channelName the channel name
     * @param payload     the message payload
     * @return {@code true} if the message was handled, {@code false} otherwise
     */
    protected boolean handleIncomingMessage(final @NotNull String channelName, final byte @NotNull [] payload) {
        ByteArrayDataInput input = ByteStreams.newDataInput(payload);
        String message = input.readUTF();
        if (handlers.containsKey(channelName)) {
            handlers.get(channelName).forEach(h -> h.handle(message));
            return true;
        } else return false;
    }

}
