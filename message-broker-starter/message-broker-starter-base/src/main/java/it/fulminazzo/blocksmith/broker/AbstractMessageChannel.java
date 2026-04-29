package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperException;
import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Abstract implementation of {@link MessageChannel} with common checks
 * and support methods.
 *
 * @param <E> the type of the {@link MessageQueryEngine} responsible for executing internal interactions
 */
@RequiredArgsConstructor
public abstract class AbstractMessageChannel<E extends MessageQueryEngine> implements MessageChannel {
    private final @NotNull Map<UUID, Function<String, String>> messageHandlers = new ConcurrentHashMap<>();
    /**
     * Identifies the sendAndReceive requests that are still pending an answer.
     */
    private final @NotNull ExpiringMap<UUID, CompletableFuture<String>> pendingResponses = ExpiringMap.lazy();
    /**
     * Identifies the messages sent by us. To prevent self-echoing.
     */
    private final @NotNull Set<UUID> sentMessages = Collections.synchronizedSet(new HashSet<>());

    /**
     * The Query engine.
     */
    protected final @NotNull E queryEngine;
    private final @NotNull Mapper mapper;

    @Override
    public @NotNull <T, R> CompletableFuture<R> sendAndReceive(final @NotNull T payload,
                                                               final @NotNull Class<R> responseType,
                                                               final @NotNull Duration timeout) {
        return sendAndReceive(payload, responseType, timeout.toMillis());
    }

    @Override
    public @NotNull <T, R> CompletableFuture<R> sendAndReceive(final @NotNull T payload,
                                                               final @NotNull Class<R> responseType,
                                                               final long timeout) {
        return sendAndReceiveRaw(mapper.serialize(payload), timeout)
                .thenApply(r -> mapper.deserialize(r, responseType));
    }

    @Override
    public @NotNull CompletableFuture<String> sendAndReceiveRaw(final @NotNull String payload, final @NotNull Duration timeout) {
        return sendAndReceiveRaw(payload, timeout.toMillis());
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
    public @NotNull UUID subscribeRaw(final @NotNull Function<String, String> consumer) {
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
    public void close() throws IOException {
        queryEngine.close();
    }

    /**
     * Initializes this channel.
     * Should only be called once.
     */
    protected void initialize() {
        queryEngine.listen(s -> handleMessage(s).join());
    }

    /**
     * Handles an incoming message with all the listening message handlers.
     *
     * @param message the message
     * @return a future containing all the responses
     */
    protected @NotNull CompletableFuture<Void> handleMessage(final @NotNull String message) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        try {
            NetworkMessage networkMessage = mapper.deserialize(message, NetworkMessage.class);
            if (sentMessages.remove(networkMessage.getId())) return CompletableFuture.completedFuture(null);
            UUID conversationId = networkMessage.getConversationId();
            if (pendingResponses.containsKey(conversationId)) {
                CompletableFuture<String> future = pendingResponses.get(conversationId);
                future.complete(networkMessage.getMessage());
                return CompletableFuture.completedFuture(null);
            }
            for (Function<String, String> handler : messageHandlers.values()) {
                String response = handler.apply(networkMessage.getMessage());
                if (response != null)
                    futures.add(sendRaw(new NetworkMessage(UUID.randomUUID(), conversationId, response)));
            }
        } catch (MapperException e) {
            // provide support for messages not sent through blocksmith
            for (Function<String, String> handler : messageHandlers.values()) {
                String response = handler.apply(message);
                if (response != null)
                    futures.add(sendRaw(response));
            }
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private @NotNull CompletableFuture<Void> sendRaw(final @NotNull NetworkMessage message) {
        sentMessages.add(message.getId());
        return queryEngine.publish(mapper.serialize(message));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private final static class NetworkMessage {
        @NotNull UUID id;
        /**
         * The id used to track back the flow of messages between clients.
         */
        @NotNull UUID conversationId;
        @NotNull String message;

    }

}
