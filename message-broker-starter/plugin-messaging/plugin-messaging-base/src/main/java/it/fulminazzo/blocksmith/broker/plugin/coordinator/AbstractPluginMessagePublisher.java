package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Abstract implementation of {@link PluginMessagePublisher} that stores failed messages in
 * a {@link Map} for later republications.
 *
 * @see PluginMessagePublisher
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractPluginMessagePublisher implements PluginMessagePublisher {
    private final @NotNull Map<String, Queue<byte[]>> failedMessages = new ConcurrentHashMap<>();

    /**
     * Attempts to publish a message to the channel.
     * <br>
     * Implementation of {@link #publish(String, byte[])} that will <b>not</b> store
     * messages to {@link #failedMessages}.
     *
     * @param channelName the channel
     * @param message     the message
     * @return {@code true} if the message was successfully published;
     *         {@code false} if the message could not be sent at this time
     *         (probably due to missing bridge between connections)
     */
    protected abstract boolean publishImpl(final @NotNull String channelName, final byte @NotNull [] message);

    @Override
    public boolean publish(final @NotNull String channelName, final byte @NotNull [] message) {
        if (publishImpl(channelName, message)) return true;
        else {
            failedMessages.computeIfAbsent(
                    channelName,
                    c -> new ConcurrentLinkedQueue<>()
            ).offer(message);
            return false;
        }
    }

    @Override
    public void republishFailedMessages() {
        for (Map.Entry<String, Queue<byte[]>> entry : failedMessages.entrySet()) {
            // We are creating a copy of the queue to avoid infinite recursion between this method and publish
            Queue<byte[]> queue = entry.getValue();
            Queue<byte[]> copy = new LinkedList<>(queue);
            queue.clear();
            while (!copy.isEmpty())
                publish(entry.getKey(), copy.poll());
        }
    }

}
