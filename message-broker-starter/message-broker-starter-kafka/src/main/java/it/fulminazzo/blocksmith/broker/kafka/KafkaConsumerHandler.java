package it.fulminazzo.blocksmith.broker.kafka;

import it.fulminazzo.blocksmith.util.ThreadUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * A wrapper for handling {@link KafkaConsumer} related tasks before actually consuming messages.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 * @see KafkaMessageQueryEngine
 */
abstract class KafkaConsumerHandler<K, V> implements ConsumerRebalanceListener, Closeable {
    private final @NotNull ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            ThreadUtils.ownedThreadFactory(KafkaConsumerHandler.class, true, null)
    );

    /**
     * Initial latch to wait for the first assignment.
     */
    private final @NotNull CountDownLatch latch = new CountDownLatch(1);

    private final @NotNull Consumer<K, V> consumer;

    private final @NotNull ScheduledFuture<?> consumeTask;

    /**
     * Instantiates a new Kafka consumer handler.
     *
     * @param consumerProperties the properties for the internal consumer
     * @param topics             the topics to subscribe the consumer to
     * @param assignmentWaitTime the time to wait for the first assignment
     * @param pollInterval       the time between polls
     * @throws KafkaMessageBrokerException if the assignment is not received within the specified time
     */
    protected KafkaConsumerHandler(
            final @NotNull Properties consumerProperties,
            final @NotNull Collection<String> topics,
            final long assignmentWaitTime,
            final long pollInterval
    ) {
        this.consumer = new KafkaConsumer<>(consumerProperties);

        CompletableFuture.runAsync(() -> consumer.subscribe(topics, this), scheduler).join();
        this.consumeTask = scheduler.scheduleAtFixedRate(
                () -> consumer.poll(Duration.ofMillis(pollInterval)).forEach(this::handle),
                0,
                pollInterval,
                TimeUnit.MILLISECONDS
        );

        try {
            if (latch.await(assignmentWaitTime, TimeUnit.MILLISECONDS)) return;
        } catch (InterruptedException e) {
            // re-thrown immediately after
        }
        throw KafkaMessageBrokerException.getAssignmentException(topics, assignmentWaitTime);
    }

    protected abstract void handle(final @NotNull K key, final @NotNull V value);

    protected void handle(final @NotNull ConsumerRecord<K, V> record) {
        handle(record.key(), record.value());
    }

    @SuppressWarnings({"checkstyle:NoFinalizer", "removal"})
    protected final void finalize() {
        // to prevent finalizer attacks
    }

    @Override
    public void onPartitionsAssigned(final @NotNull Collection<TopicPartition> partitions) {
        consumer.seekToEnd(partitions);
        latch.countDown();
    }

    @Override
    public void onPartitionsRevoked(final @NotNull Collection<TopicPartition> partitions) {
        // do nothing
    }

    @Override
    public void close() {
        consumeTask.cancel(true);
        CompletableFuture.runAsync(consumer::close, scheduler).join();
        scheduler.shutdown();
    }

}
