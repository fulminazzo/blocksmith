package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperException;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Abstract implementation of {@link MessageChannel} with common checks
 * and support methods.
 */
@RequiredArgsConstructor
public abstract class AbstractMessageChannel implements MessageChannel {
    private final @NotNull Map<UUID, MessageHandler> messageHandlers = new ConcurrentHashMap<>();
    /**
     * Identifies the sendAndReceive requests that are still pending an answer.
     */
    private final @NotNull Map<UUID, CompletableFuture<String>> pendingResponses = new ConcurrentHashMap<>();

    private final @NotNull Mapper mapper;

    @Override
    public @NotNull <T> CompletableFuture<Void> send(final @NotNull T payload) {
        return sendRaw(mapper.serialize(payload));
    }

    @Override
    public @NotNull CompletableFuture<Void> sendRaw(final @NotNull String payload) {
        return sendRaw(new NetworkMessage(UUID.randomUUID(), payload));
    }

    @Override
    public @NotNull <T, R> UUID subscribe(final @NotNull Class<T> messageType, final @NotNull Function<T, R> consumer) {
        return subscribeRaw(s -> {
            R result = consumer.apply(mapper.deserialize(s, messageType));
            return result == null ? null : mapper.serialize(result);
        });
    }

    @Override
    public @NotNull UUID subscribeRaw(final @NotNull MessageHandler consumer) {
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
        //TODO: implement
        throw new UnsupportedOperationException();
    }

    /**
     * Handles an incoming message with all the listening message handlers.
     *
     * @param message the message
     */
    protected void handleMessage(final @NotNull String message) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        try {
            NetworkMessage networkMessage = mapper.deserialize(message, NetworkMessage.class);
            UUID conversationId = networkMessage.getConversationId();
            if (pendingResponses.containsKey(conversationId)) {
                CompletableFuture<String> future = pendingResponses.remove(conversationId);
                future.complete(networkMessage.getMessage());
                return;
            }
            for (MessageHandler handler : messageHandlers.values()) {
                String response = handler.handle(networkMessage.getMessage());
                if (response != null)
                    futures.add(sendRaw(new NetworkMessage(conversationId, response)));
            }
        } catch (MapperException e) {
            // provide support for messages not sent through blocksmith
            for (MessageHandler handler : messageHandlers.values()) {
                String response = handler.handle(message);
                if (response != null)
                    futures.add(sendRaw(response));
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private @NotNull CompletableFuture<Void> sendRaw(final @NotNull NetworkMessage message) {
        return sendRawImpl(mapper.serialize(message));
    }

    /**
     * Sends a raw message to the channel.
     *
     * @param payload the payload to send
     * @return nothing
     */
    protected abstract @NotNull CompletableFuture<Void> sendRawImpl(final @NotNull String payload);

    @Value
    private static class NetworkMessage {
        /**
         * The id used to track back the flow of messages between clients.
         */
        @NotNull UUID conversationId;
        @NotNull String message;

    }

}
