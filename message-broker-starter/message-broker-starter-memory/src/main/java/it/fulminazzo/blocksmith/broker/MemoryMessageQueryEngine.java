package it.fulminazzo.blocksmith.broker;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Pseudo-implementation of a Message query engine for in-memory messages.
 */
@RequiredArgsConstructor
public final class MemoryMessageQueryEngine implements MessageQueryEngine {
    private final @NotNull String channelName;

    private final @NotNull Executor executor;

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(() -> getChannel().publish(payload), executor);
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        getChannel().register(this, consumer);
    }

    @Override
    public void close() {
        getChannel().unregister(this);
    }

    private @NotNull MemoryChannel getChannel() {
        return MemoryChannel.getChannel(channelName);
    }

    /**
     * Artificial channel implementation.
     */
    @RequiredArgsConstructor
    static final class MemoryChannel {
        private static final @NotNull Map<String, MemoryChannel> CHANNELS = new ConcurrentHashMap<>();

        private final @NotNull String name;
        private final @NotNull Map<MemoryMessageQueryEngine, Consumer<String>> listeners = new ConcurrentHashMap<>();

        /**
         * Publishes the given message to all the listeners.
         * If a listener is not listening, they will <b>lose</b> the message.
         *
         * @param payload the message to publish
         */
        public void publish(final @NotNull String payload) {
            listeners.values().forEach(consumer -> consumer.accept(payload));
        }

        /**
         * Registers a listener on the channel.
         *
         * @param engine   the engine to register the listener on
         * @param consumer the function to handle the message
         */
        public void register(final @NotNull MemoryMessageQueryEngine engine, final @NotNull Consumer<String> consumer) {
            listeners.put(engine, consumer);
        }

        /**
         * Unregisters a listener from the channel.
         *
         * @param engine the engine to unregister the listener from
         */
        public void unregister(final @NotNull MemoryMessageQueryEngine engine) {
            listeners.remove(engine);
        }

        /**
         * Gets the channel with the given name.
         *
         * @param name the name of the channel
         * @return the channel
         */
        public static @NotNull MemoryChannel getChannel(final @NotNull String name) {
            return CHANNELS.computeIfAbsent(name, MemoryChannel::new);
        }

    }

}
