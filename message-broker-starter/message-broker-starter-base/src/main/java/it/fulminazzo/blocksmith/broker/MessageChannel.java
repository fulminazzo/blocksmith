package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A general channel for handling messaging across servers.
 */
public interface MessageChannel {

    /**
     * Sends a message to the channel.
     *
     * @param <T>     the type of the payload
     * @param payload the payload to send
     * @return nothing
     */
    <T> @NotNull CompletableFuture<Void> send(final @NotNull T payload);

    /**
     * Sends a raw message to the channel.
     *
     * @param payload the payload to send
     * @return nothing
     */
    @NotNull CompletableFuture<Void> sendRaw(final @NotNull String payload);

    /**
     * Adds a new handler for incoming messages in the channel.
     *
     * @param <T>         the type of the message
     * @param <R>         the type of the response
     * @param messageType the type to which the message should be converted
     * @param consumer    the function to handle the message.
     *                    If the return is not {@code null}, the message is sent back into the channel.
     * @return the id of the handler
     */
    <T, R> @NotNull UUID subscribe(final @NotNull Class<T> messageType, final @NotNull Function<T, R> consumer);

    /**
     * Adds a new handler for incoming messages in the channel.
     *
     * @param consumer the function to handle the message.
     *                 If the return is not {@code null}, the message is sent back into the channel.
     * @return the id of the handler
     */
    @NotNull UUID subscribeRaw(final @NotNull MessageHandler consumer);

    /**
     * Unsubscribes a handler from the channel.
     *
     * @param id the id of the handler
     * @return this object (for method chaining)
     */
    @NotNull MessageChannel unsubscribe(final @NotNull UUID id);

    /**
     * Sends a message to the channel.
     * Then, awaits for the response and returns it.
     *
     * @param <T>          the type of the payload
     * @param <R>          the type of the response
     * @param payload      the payload to send
     * @param responseType the type of the response
     * @param timeout      the timeout after which the task will fail
     * @return the response
     */
    <T, R> @NotNull CompletableFuture<R> sendAndReceive(final @NotNull T payload,
                                                        final @NotNull Class<R> responseType,
                                                        final @NotNull Duration timeout);

    /**
     * Sends a message to the channel.
     * Then, awaits for the response and returns it.
     *
     * @param <T>          the type of the payload
     * @param <R>          the type of the response
     * @param payload      the payload to send
     * @param responseType the type of the response
     * @param timeout      the timeout after which the task will fail (in milliseconds)
     * @return the response
     */
    <T, R> @NotNull CompletableFuture<R> sendAndReceive(final @NotNull T payload,
                                                        final @NotNull Class<R> responseType,
                                                        final long timeout);

    /**
     * Sends a message to the channel.
     * Then, awaits for the response and returns it.
     *
     * @param payload the payload to send
     * @param timeout the timeout after which the task will fail
     * @return the response
     */
    @NotNull CompletableFuture<String> sendAndReceiveRaw(final @NotNull String payload, final @NotNull Duration timeout);

    /**
     * Sends a message to the channel.
     * Then, awaits for the response and returns it.
     *
     * @param payload the payload to send
     * @param timeout the timeout after which the task will fail (in milliseconds)
     * @return the response
     */
    @NotNull CompletableFuture<String> sendAndReceiveRaw(final @NotNull String payload, final long timeout);

}
