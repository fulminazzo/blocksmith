package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Base interface for message query engines.
 * <br>
 * A MessageQueryEngine encapsulates all backend-specific resources and operations needed
 * to interact with a particular message broker system.
 * Each backend implements this interface with its own specific methods and resources.
 */
public interface MessageQueryEngine extends Closeable {

    /**
     * Publishes a raw serialized payload to the underlying message broker.
     *
     * @param payload the payload to send
     * @return nothing
     */
    @NotNull CompletableFuture<Void> publish(final @NotNull String payload);

    /**
     * Registers a raw listener on the underlying message broker.
     * The consumer will be called on every incoming message.
     *
     * @param consumer the function to handle the message
     */
    void listen(final @NotNull Consumer<String> consumer);

}
