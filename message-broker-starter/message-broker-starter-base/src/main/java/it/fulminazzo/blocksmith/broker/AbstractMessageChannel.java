package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
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
    protected final @NotNull Map<UUID, MessageHandler> messageHandlers = new ConcurrentHashMap<>();

    private final @NotNull Mapper mapper;

    @Override
    public @NotNull <T> CompletableFuture<Void> send(final @NotNull T payload) {
        return sendRaw(mapper.serialize(payload));
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

}
