package it.fulminazzo.blocksmith.broker.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.broker.MessageQueryEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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

    private final @NotNull RabbitMQMessageChannelSettings.QueueSettings queueSettings;

    private final @NotNull AtomicInteger consumerCount = new AtomicInteger();
    private final int engineId;

    /**
     * Instantiates a new RabbitMQ message query engine.
     *
     * @param executor      the executor
     * @param channelName   the channel name
     * @param channel       the channel
     * @param routingKey    the routing key ({@code null} if irrelevant)
     * @param queueSettings the queue settings
     */
    RabbitMQMessageQueryEngine(
            final @NotNull ExecutorService executor,
            final @NotNull String channelName,
            final @NotNull Channel channel,
            final @Nullable String routingKey,
            final @NotNull RabbitMQMessageChannelSettings.QueueSettings queueSettings
    ) {
        super(channelName);
        this.engineId = ENGINE_COUNT.getAndIncrement();
        this.executor = executor;
        this.channel = channel;
        this.routingKey = routingKey == null ? "" : routingKey;
        this.queueSettings = queueSettings;
    }

    private @NotNull String getConsumerTag() {
        return getConsumerTag(consumerCount.getAndIncrement());
    }

    private @NotNull String getConsumerTag(final int consumerCount) {
        return String.format("%s-rabbitmq-%d-consumer-%d", ProjectInfo.PROJECT_NAME, engineId, consumerCount);
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
                        throw RabbitMQMessageBrokerException.publishException(payload, e);
                    }
                },
                executor
        );
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        final String queueName = queueSettings.getQueueName();
        try {
            channel.queueDeclare(
                    queueName,
                    queueSettings.isDurable(),
                    queueSettings.isExclusive(),
                    queueSettings.isAutoDelete(),
                    queueSettings.getArguments()
            );
            channel.queueBind(queueName, getChannelName(), routingKey);
        } catch (IOException e) {
            throw RabbitMQMessageBrokerException.queueDeclareException(
                    queueName,
                    getChannelName(),
                    routingKey,
                    e
            );
        }
        try {
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
                            String payload = new String(body, StandardCharsets.UTF_8);
                            consumer.accept(payload);
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        }

                    }
            );
        } catch (IOException e) {
            throw RabbitMQMessageBrokerException.registerConsumerException(queueName, e);
        }
    }

    @Override
    public void close() {
        if (channel.isOpen()) {
            final int count = consumerCount.get();
            for (int i = 0; i < count; i++)
                try {
                    channel.basicCancel(getConsumerTag(i));
                } catch (IOException e) {
                    // Ignored to allow other consumers to close
                }
            try {
                channel.close();
            } catch (TimeoutException | IOException ignored) {
                // Ignored
            }
        }
    }

}
