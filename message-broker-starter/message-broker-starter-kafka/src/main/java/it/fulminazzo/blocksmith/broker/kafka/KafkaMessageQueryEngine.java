package it.fulminazzo.blocksmith.broker.kafka;

import it.fulminazzo.blocksmith.broker.MessageQueryEngine;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * A message query engine with Kafka support.
 * <br>
 * The internal producer and consumer will be configured with the provided properties.
 * The <b>channel name</b> will be used as the <b>topic name</b>.
 *
 * @see KafkaMessageChannel
 * @see KafkaMessageBroker
 */
public final class KafkaMessageQueryEngine extends MessageQueryEngine {
    private static final @NotNull String SERIALIZER = StringSerializer.class.getCanonicalName();
    private static final @NotNull String DESERIALIZER = StringDeserializer.class.getCanonicalName();

    private final @NotNull List<KafkaConsumerHandler<String, String>> consumers = new CopyOnWriteArrayList<>();

    private final @NotNull ExecutorService executor;

    private final @NotNull KafkaProducer<String, String> producer;

    private final @Nullable String key;

    private final @NotNull Properties properties;
    private final long assignmentWaitTime;
    private final long pollInterval;

    /**
     * Instantiates a new Kafka message query engine.
     *
     * @param executor           the executor
     * @param properties         the properties to apply to both the producer and the consumer
     * @param channelName        the channel name
     * @param key                the key to include in the messages
     * @param assignmentWaitTime the time to wait for the first assignment
     * @param pollInterval       the time between polls
     */
    public KafkaMessageQueryEngine(
            final @NotNull ExecutorService executor,
            final @NotNull Properties properties,
            final @NotNull String channelName,
            final @Nullable String key,
            final long assignmentWaitTime,
            final long pollInterval
    ) {
        super(channelName);
        this.executor = executor;

        this.properties = new Properties();
        this.properties.putAll(properties);
        this.assignmentWaitTime = assignmentWaitTime;
        this.pollInterval = pollInterval;

        this.properties.put("key.serializer", SERIALIZER);
        this.properties.put("value.serializer", SERIALIZER);
        this.properties.put("key.deserializer", DESERIALIZER);
        this.properties.put("value.deserializer", DESERIALIZER);

        this.producer = new KafkaProducer<>(this.properties);

        this.key = key;
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(() -> {
            String topic = getChannelName();
            try {
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, payload);
                producer.send(producerRecord).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw KafkaMessageBrokerException.publishException(topic, payload, e);
            } catch (ExecutionException e) {
                throw KafkaMessageBrokerException.publishException(topic, payload, e);
            }
        }, executor);
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        consumers.add(new KafkaConsumerHandlerImpl<>(
                properties,
                List.of(getChannelName()),
                assignmentWaitTime,
                pollInterval,
                consumer
        ));
    }

    @Override
    public void close() {
        consumers.forEach(KafkaConsumerHandler::close);
        producer.close();
    }

    private static final class KafkaConsumerHandlerImpl<K, V> extends KafkaConsumerHandler<K, V> {
        private final @NotNull Consumer<V> messageConsumer;

        /**
         * Instantiates a new Kafka consumer handler.
         *
         * @param consumerProperties the properties for the internal consumer
         * @param topics             the topics to subscribe the consumer to
         * @param assignmentWaitTime the time to wait for the first assignment
         * @param pollInterval       the time between polls
         * @param messageConsumer    the function to parse the message
         * @throws KafkaMessageBrokerException if the assignment is not received within the specified time
         */
        KafkaConsumerHandlerImpl(
                final @NotNull Properties consumerProperties,
                final @NotNull List<String> topics,
                final long assignmentWaitTime,
                final long pollInterval,
                final @NotNull Consumer<V> messageConsumer
        ) {
            super(consumerProperties, topics, assignmentWaitTime, pollInterval);
            this.messageConsumer = messageConsumer;
        }

        @Override
        protected void handle(final @NotNull K key, final @NotNull V value) {
            messageConsumer.accept(value);
        }

    }

}
