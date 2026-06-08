package it.fulminazzo.blocksmith.broker;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperException;
import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap;
import it.fulminazzo.blocksmith.structure.expiring.ExpiringSet;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Abstract implementation of {@link MessageChannel} with common checks
 * and support methods.
 *
 * @param <E> the type of the {@link MessageQueryEngine} responsible for executing internal interactions
 * @see MessageChannel
 * @see MessageQueryEngine
 */
public abstract class AbstractMessageChannel<E extends MessageQueryEngine> implements MessageChannel {
    private static final long SELF_ECHO_TIMEOUT = 10_000;

    /**
     * The Query engine.
     */
    protected final @NotNull E queryEngine;

    private final @NotNull Map<UUID, UnaryOperator<String>> messageHandlers = new ConcurrentHashMap<>();
    /**
     * Identifies the sendAndReceive requests that are still pending an answer.
     */
    private final @NotNull ExpiringMap<UUID, CompletableFuture<String>> pendingResponses = ExpiringMap.lazy();
    /**
     * Identifies the messages sent by us. To prevent self-echoing.
     */
    private final @NotNull ExpiringSet<UUID> sentMessages = ExpiringSet.lazy();
    private final @NotNull Mapper mapper;

    @Getter
    private boolean closed;

    /**
     * Instantiates a new Abstract message channel.
     *
     * @param queryEngine the query engine
     * @param mapper      the mapper
     */
    protected AbstractMessageChannel(
            final @NotNull E queryEngine,
            final @NotNull Mapper mapper
    ) {
        this.queryEngine = queryEngine;
        this.mapper = mapper;
        queryEngine.listen(this::handleMessage);
    }

    /**
     * Handles an incoming message with all the listening message handlers.
     *
     * @param message the message
     */
    @SuppressFBWarnings({"DE_MIGHT_IGNORE", "REC_CATCH_EXCEPTION"})
    protected void handleMessage(final @NotNull String message) {
        try {
            NetworkMessage networkMessage = mapper.deserialize(message, NetworkMessage.class);
            if (sentMessages.remove(networkMessage.getId())) return;
            UUID conversationId = networkMessage.getConversationId();
            if (pendingResponses.containsKey(conversationId)) {
                CompletableFuture<String> future = pendingResponses.get(conversationId);
                future.complete(networkMessage.getMessage());
                return;
            }
            for (UnaryOperator<String> handler : messageHandlers.values())
                try {
                    String response = handler.apply(networkMessage.getMessage());
                    if (response != null)
                        sendRaw(new NetworkMessage(UUID.randomUUID(), conversationId, response));
                } catch (Exception ignored) {
                    // Failed handling, maybe due to parsing. Ignoring to allow other handlers to proceed.
                }
        } catch (MapperException e) {
            // provide support for messages not sent through blocksmith
            for (UnaryOperator<String> handler : messageHandlers.values())
                try {
                    String response = handler.apply(message);
                    if (response != null) sendRaw(response);
                } catch (Exception ignored) {
                    // Failed handling, maybe due to parsing. Ignoring to allow other handlers to proceed.
                }
        }
    }

    private @NotNull CompletableFuture<Void> sendRaw(final @NotNull NetworkMessage message) {
        sentMessages.add(message.getId(), SELF_ECHO_TIMEOUT);
        return queryEngine.publish(mapper.serialize(message));
    }

    @Override
    public @NotNull <T, R> CompletableFuture<R> sendAndReceive(
            final @NotNull T payload,
            final @NotNull Class<R> responseType,
            final long timeout
    ) {
        return sendAndReceiveRaw(mapper.serialize(payload), timeout)
                .thenApply(r -> mapper.deserialize(r, responseType));
    }

    @Override
    public @NotNull <T, R> CompletableFuture<R> sendAndReceive(
            final @NotNull T payload,
            final @NotNull Class<R> responseType,
            final @NotNull Duration timeout
    ) {
        return sendAndReceive(payload, responseType, timeout.toMillis());
    }

    @Override
    public @NotNull CompletableFuture<String> sendAndReceiveRaw(final @NotNull String payload, final long timeout) {
        NetworkMessage message = new NetworkMessage(UUID.randomUUID(), UUID.randomUUID(), payload);
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingResponses.put(message.getConversationId(), future, timeout);
        return sendRaw(message)
                .thenCompose(v -> future.orTimeout(timeout, TimeUnit.MILLISECONDS))
                .whenComplete((r, t) -> pendingResponses.remove(message.getConversationId()));
    }

    @Override
    public @NotNull CompletableFuture<String> sendAndReceiveRaw(
            final @NotNull String payload,
            final @NotNull Duration timeout
    ) {
        return sendAndReceiveRaw(payload, timeout.toMillis());
    }

    @Override
    public @NotNull <T> CompletableFuture<Void> send(final @NotNull T payload) {
        return sendRaw(mapper.serialize(payload));
    }

    @Override
    public @NotNull CompletableFuture<Void> sendRaw(final @NotNull String payload) {
        return sendRaw(new NetworkMessage(UUID.randomUUID(), UUID.randomUUID(), payload));
    }

    @Override
    public @NotNull <T> UUID subscribe(final @NotNull Class<T> messageType, final @NotNull Consumer<T> consumer) {
        return subscribe(messageType, t -> {
            consumer.accept(t);
            return null;
        });
    }

    @Override
    public @NotNull <T, R> UUID subscribe(final @NotNull Class<T> messageType, final @NotNull Function<T, R> consumer) {
        return subscribeRaw(s -> {
            R result = consumer.apply(mapper.deserialize(s, messageType));
            return result == null ? null : mapper.serialize(result);
        });
    }

    @Override
    public @NotNull UUID subscribeRaw(final @NotNull Consumer<String> consumer) {
        return subscribeRaw(s -> {
            consumer.accept(s);
            return null;
        });
    }

    @Override
    public @NotNull UUID subscribeRaw(final @NotNull UnaryOperator<String> consumer) {
        UUID id = UUID.randomUUID();
        messageHandlers.put(id, consumer);
        return id;
    }

    @Override
    public @NotNull MessageChannel unsubscribe(final @NotNull UUID id) {
        messageHandlers.remove(id);
        return this;
    }

    @Override
    public void close() {
        if (!isClosed()) {
            queryEngine.close();
            closed = true;
        }
    }

    /**
     * DTO for a message over the network.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    protected static final class NetworkMessage implements Serializable {
        private static final long serialVersionUID = 5257190224662114674L;

        @NotNull UUID id;
        /**
         * The id used to track back the flow of messages between clients.
         */
        @NotNull UUID conversationId;
        @NotNull String message;

    }

}
