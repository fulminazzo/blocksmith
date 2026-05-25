package it.fulminazzo.blocksmith.broker.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.broker.MessageQueryEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A message query engine with RabbitMQ support.
 * <br>
 * Uses the <a href="https://www.rabbitmq.com/java-client.html">RabbitMQ Java Client</a>.
 * <br>
 * The internal channel will be configured as follows:
 * <ul>
 *     <li>the <b>channel name</b> becomes the <b>exchange name</b>;</li>
 *     <li>if the channel type is {@link MessageChannelType#DIRECT}, the <b>subchannel name</b> becomes the
 *     <b>routing key</b> (otherwise it will be considered irrelevant);</li>
 *     <li>any <b>consumer tag</b> will be generated automatically;</li>
 *     <li>finally, the <b>queue name</b> is provided from the user.</li>
 * </ul>
 *
 * @see RabbitMQMessageChannel
 * @see RabbitMQMessageBroker
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class RabbitMQMessageQueryEngine extends MessageQueryEngine {
    private static final @NotNull AtomicInteger ENGINE_COUNT = new AtomicInteger();

    private final @NotNull ExecutorService executor;

    private final @NotNull Channel channel;

    private final @NotNull String routingKey;
    private final @NotNull String queueName;

    private final int engineId;
    private volatile int consumerCount = 0;

    /**
     * Instantiates a new RabbitMQ message query engine.
     *
     * @param channelName the channel name
     * @param executor    the executor
     * @param channel     the channel
     * @param routingKey  the routing key ({@code null} if irrelevant)
     * @param queueName   the queue name
     */
    RabbitMQMessageQueryEngine(
            final @NotNull ExecutorService executor,
            final @NotNull String channelName,
            final @NotNull Channel channel,
            final @Nullable String routingKey,
            final @NotNull String queueName
    ) {
        super(channelName);
        this.engineId = ENGINE_COUNT.getAndIncrement();
        this.executor = executor;
        this.channel = channel;
        this.routingKey = routingKey == null ? "" : routingKey;
        this.queueName = queueName;
    }

    private synchronized @NotNull String getConsumerTag() {
        int consumerCount = this.consumerCount++;
        return getConsumerTag(consumerCount);
    }

    private @NotNull String getConsumerTag(final int consumerCount) {
        return String.format("rabbitmq-engine-%d-consumer-%d", engineId, consumerCount);
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        channel.basicPublish(
                                getChannelName(),
                                routingKey,
                                null,
                                payload.getBytes(StandardCharsets.UTF_8)
                        );
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                },
                executor
        );
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        try {
            //TODO: export settings
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicConsume(
                    queueName,
                    false,
                    getConsumerTag(),
                    new DefaultConsumer(channel) {

                        @Override
                        public void handleDelivery(
                                final String consumerTag,
                                final Envelope envelope,
                                final AMQP.BasicProperties properties,
                                final byte[] body
                        ) throws IOException {
                            consumer.accept(new String(body, StandardCharsets.UTF_8));
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        }

                    }
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (channel.isOpen()) {
            final int count = consumerCount;
            for (int i = 0; i < count; i++) channel.basicCancel(getConsumerTag(i));
            try {
                channel.close();
            } catch (TimeoutException ignored) {
                // Ignored
            }
        }
        executor.shutdown();
    }

}
