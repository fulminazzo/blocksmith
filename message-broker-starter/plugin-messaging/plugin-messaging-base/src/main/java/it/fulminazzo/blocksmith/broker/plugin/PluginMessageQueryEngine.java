package it.fulminazzo.blocksmith.broker.plugin;

import it.fulminazzo.blocksmith.broker.MessageQueryEngine;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * A message query engine with support for the Minecraft plugin messaging system.
 * <br>
 * Provides utility methods to help implementations focus on the main functioning of the engine.
 */
public abstract class PluginMessageQueryEngine extends MessageQueryEngine {
    private final @NotNull List<Consumer<String>> listeners = Collections.synchronizedList(new ArrayList<>());
    /**
     * If a message fails to be published due to a lack of online players,
     * it will be stored here and published when the server is back online.
     */
    private final @NotNull Queue<byte[]> pendingMessages = new ConcurrentLinkedQueue<>();

    protected final @NotNull PluginMessageRegistrar registrar;

    /**
     * Instantiates a new Plugin message query engine.
     *
     * @param channelName the channel name.
     *                    <b>NOTE</b>: implementations will <b>prepend</b> the {@link #registrar}
     *                    name automatically
     * @param registrar   the registrar for the internal registration of channels
     */
    protected PluginMessageQueryEngine(final @NotNull String channelName, final @NotNull PluginMessageRegistrar registrar) {
        super(registrar.lowercaseName() + ":" + channelName);
        this.registrar = registrar;
    }

    /**
     * Publishes a raw serialized payload to the underlying message broker.
     *
     * @param payload the payload to send
     * @return {@code true} if the message was published successfully, {@code false} otherwise.
     */
    protected abstract boolean publish(final byte @NotNull [] payload);

    /**
     * Handles an incoming raw message.
     *
     * @param data the data received
     */
    protected void handleMessage(final byte @NotNull [] data) {
        String payload = new String(data, StandardCharsets.UTF_8);
        listeners.forEach(l -> l.accept(payload));
    }

    /**
     * Attempts to re-send all pending messages.
     */
    protected void resendPendingMessages() {
        while (!pendingMessages.isEmpty())
            if (!publish(pendingMessages.poll())) break;
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        if (!publish(data)) pendingMessages.add(data);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        listeners.add(consumer);
    }

    @Override
    public void close() {
        listeners.clear();
    }

}
