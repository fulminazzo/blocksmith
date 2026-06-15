package it.fulminazzo.blocksmith.broker.plugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.fulminazzo.blocksmith.broker.MessageQueryEngine;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinator;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * A message query engine with support for the Minecraft plugin messaging system.
 *
 * @see PluginMessageChannel
 * @see PluginMessageBroker
 * @see PluginMessageChannelCoordinator
 */
public final class PluginMessageQueryEngine extends MessageQueryEngine implements PluginMessageHandler {
    private final @NotNull List<Consumer<String>> consumers = new ArrayList<>();

    private final @NotNull ExecutorService executor;

    private final @NotNull PluginMessageChannelCoordinator coordinator;

    /**
     * Instantiates a new Plugin message query engine.
     *
     * @param executor    the executor
     * @param channelName the channel name
     * @param coordinator the coordinator
     */
    PluginMessageQueryEngine(
            final @NotNull ExecutorService executor,
            final @NotNull String channelName,
            final @NotNull PluginMessageChannelCoordinator coordinator
    ) {
        super(channelName);
        this.executor = executor;
        this.coordinator = coordinator;

        coordinator.registerHandler(channelName, this);
    }

    @Override
    public void handle(final @NotNull String message) {
        consumers.forEach(c -> c.accept(message));
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(
                () -> {
                    ByteArrayDataOutput output = ByteStreams.newDataOutput();
                    output.writeUTF(payload);
                    coordinator.publish(getChannelName(), output.toByteArray());
                },
                executor
        );
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        consumers.add(consumer);
    }

    @Override
    public void close() {
        coordinator.unregisterHandler(this);
    }

}
