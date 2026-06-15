package it.fulminazzo.blocksmith.broker.kafka;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Message channel settings Kafka channels.
 *
 * @see KafkaMessageChannel
 * @see KafkaMessageBroker
 */
@Getter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class KafkaMessageChannelSettings extends MessageChannelSettings<KafkaMessageChannelSettings> {
    static final @NotNull String ACKS = "acks";
    static final @NotNull String RETRIES = "retries";
    static final @NotNull String COMPRESSION_TYPE = "compression.type";
    static final @NotNull String ENABLE_IDEMPOTENCE = "enable.idempotence";
    static final @NotNull String DELIVERY_TIMEOUT = "delivery.timeout.ms";
    static final @NotNull String GROUP_ID = "group.id";
    static final @NotNull String AUTO_OFFSET_RESET = "auto.offset.reset";
    static final @NotNull String ENABLE_AUTO_COMMIT = "enable.auto.commit";
    static final @NotNull String MAX_POLL_RECORDS = "max.poll.records";
    static final @NotNull String AUTO_COMMIT_INTERVAL = "auto.commit.interval.ms";
    static final @NotNull String MAX_POLL_INTERVAL = "max.poll.interval.ms";
    static final @NotNull String SESSION_TIMEOUT = "session.timeout.ms";
    static final @NotNull String HEARTBEAT_INTERVAL = "heartbeat.interval.ms";

    @Getter(AccessLevel.NONE)
    private final @NotNull Map<String, Object> properties = new HashMap<>();

    private long assignmentWaitTime = 60_000L;
    private long pollInterval = 125L;

    private @Nullable String messagesKey;

    /**
     * Instantiates a new Kafka message channel settings.
     */
    public KafkaMessageChannelSettings() {
        withAcknowledgeMode(AcknowledgeMode.FIRE_AND_FORGET)
                .withSendRetries(0)
                .withCompressionType(CompressionType.NONE)
                .addProperty(ENABLE_IDEMPOTENCE, false)
                .withDeliveryTimeout(2 * 60_000)
                .withOffsetResetStrategy(OffsetResetStrategy.LATEST)
                .addProperty(ENABLE_AUTO_COMMIT, false)
                .withMaxPollRecords(500)
                .withAutoCommitInterval(5_000)
                .withMaxPollInterval(300_000)
                .withSessionTimeout(45_000)
                .withHeartbeatInterval(3_000);
    }

    /**
     * Builds a {@link Properties} object from the current settings.
     *
     * @return the properties
     */
    public @NotNull Properties buildProperties() {
        Properties finalProperties = new Properties();
        finalProperties.putAll(this.properties);
        finalProperties.put(GROUP_ID, getGroupId());
        return finalProperties;
    }

    /**
     * Sets the time to wait for the first assignment.
     *
     * @param assignmentWaitTime the time (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withAssignmentWaitTime(final long assignmentWaitTime) {
        this.assignmentWaitTime = assignmentWaitTime;
        return this;
    }

    /**
     * Sets the interval between polls.
     *
     * @param pollInterval the interval (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withPollInterval(final long pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }

    /*
     * PRODUCER
     */

    /**
     * Enables idempotence with the default settings.
     * Alias for:
     * <pre>{@code
     * new KafkaMessageChannelSettings()
     *         .enableIdempotence()
     *         .withAcknowledgeMode(AcknowledgeMode.ALL)
     *         .withSendRetries(Integer.MAX_VALUE);
     * }</pre>
     *
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings idempotenceWithDefaults() {
        return enableIdempotence()
                .withAcknowledgeMode(AcknowledgeMode.ALL)
                .withSendRetries(Integer.MAX_VALUE);
    }

    /**
     * Sets how many ACKs the producer should receive for a successful sent message.
     *
     * @param acknowledgeMode the ACK mode
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withAcknowledgeMode(final @NotNull AcknowledgeMode acknowledgeMode) {
        return addProperty(ACKS, acknowledgeMode.getValue());
    }

    /**
     * Sets the number of retries to send a message before failing.
     *
     * @param sendRetries the retries
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withSendRetries(final int sendRetries) {
        return addProperty(RETRIES, sendRetries);
    }

    /**
     * Sets the compression algorithm to use when sending messages.
     *
     * @param compressionType the compression type
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withCompressionType(final @NotNull CompressionType compressionType) {
        return addProperty(COMPRESSION_TYPE, compressionType.name().toLowerCase(Locale.ROOT));
    }

    /**
     * Ensures that if the producer sends the same message twice, it is not stored again in the topic.
     * Requires:
     * <ul>
     *     <li>{@link #withAcknowledgeMode(AcknowledgeMode)} to be {@link AcknowledgeMode#ALL};</li>
     *     <li>{@link #withSendRetries(int)} to be greater than {@code 0}.</li>
     * </ul>
     *
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings enableIdempotence() {
        return addProperty(ENABLE_IDEMPOTENCE, true);
    }

    /**
     * Sets the timeout for the delivery of a message.
     *
     * @param deliveryTimeout the timeout (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withDeliveryTimeout(final int deliveryTimeout) {
        return addProperty(DELIVERY_TIMEOUT, deliveryTimeout);
    }

    /**
     * Sets the key to include in the payload of the messages.
     *
     * @param messagesKey the key
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withMessagesKey(final @NotNull String messagesKey) {
        this.messagesKey = messagesKey;
        return this;
    }

    /*
     * CONSUMER
     */

    /**
     * Sets the group id for the consumer.
     *
     * @param groupId the group id
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withGroupId(final @NotNull String groupId) {
        return addProperty(GROUP_ID, groupId);
    }

    /**
     * Sets the {@link OffsetResetStrategy} to use when the consumer is started.
     *
     * @param offsetResetStrategy the offset reset strategy
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withOffsetResetStrategy(
            final @NotNull OffsetResetStrategy offsetResetStrategy
    ) {
        return addProperty(AUTO_OFFSET_RESET, offsetResetStrategy.name().toLowerCase(Locale.ROOT));
    }

    /**
     * Enables auto-committing of messages and sets the interval between each commit.
     * Alias for:
     * <pre>{@code
     * new KafkaMessageChannelSettings()
     *         .enableAutoCommit()
     *         .withAutoCommitInterval(autoCommitInterval);
     * }</pre>
     *
     * @param autoCommitInterval the interval (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withAutoCommit(final int autoCommitInterval) {
        return enableAutoCommit().withAutoCommitInterval(autoCommitInterval);
    }

    /**
     * Enables auto-committing of messages.
     *
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings enableAutoCommit() {
        return addProperty(ENABLE_AUTO_COMMIT, true);
    }

    /**
     * Sets the maximum number of messages received from one poll.
     *
     * @param maxPollRecords the maximum number of messages
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withMaxPollRecords(final int maxPollRecords) {
        return addProperty(MAX_POLL_RECORDS, maxPollRecords);
    }

    /**
     * Sets the interval between a successful poll and the automatic committing of its messages.
     * Requires {@link #enableAutoCommit()}.
     *
     * @param autoCommitInterval the interval (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withAutoCommitInterval(final int autoCommitInterval) {
        return addProperty(AUTO_COMMIT_INTERVAL, autoCommitInterval);
    }

    /**
     * Sets the maximum time between polls before the consumer is considered dead.
     * Should be greater than {@link #withPollInterval(long)}.
     *
     * @param maxPollInterval the maximum time (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withMaxPollInterval(final int maxPollInterval) {
        return addProperty(MAX_POLL_INTERVAL, maxPollInterval);
    }

    /**
     * Sets the session timeout and heartbeat interval.
     * Alias for:
     * <pre>{@code
     * new KafkaMessageChannelSettings()
     *         .withSessionTimeout(sessionTimeout)
     *         .withHeartbeatInterval(sessionTimeout / 3);
     * }</pre>
     *
     * @param sessionTimeout the session timeout
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withSessionAndHeartbeat(final int sessionTimeout) {
        return withSessionTimeout(sessionTimeout)
                .withHeartbeatInterval(sessionTimeout / 3);
    }

    /**
     * Sets the maximum time after which the consumer is considered unresponsive from the broker.
     *
     * @param sessionTimeout the timeout (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withSessionTimeout(final int sessionTimeout) {
        return addProperty(SESSION_TIMEOUT, sessionTimeout);
    }

    /**
     * Sets the interval between heartbeats sent by the consumer.
     * Should be a third of {@link #withSessionTimeout(int)}.
     *
     * @param heartbeatInterval the interval (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings withHeartbeatInterval(final int heartbeatInterval) {
        return addProperty(HEARTBEAT_INTERVAL, heartbeatInterval);
    }

    /**
     * Adds a new general property to the settings.
     * <br>
     * <b>WARNING</b>: using this method instead of dedicated ones is allowed but not recommended.
     * <br>
     * Bad:
     * <pre>{@code
     * new KafkaMessageChannelSettings().addProperty("group.id", "my_group");
     * }</pre>
     * Good:
     * <pre>{@code
     * new KafkaMessageChannelSettings().withGroupId("my_group");
     * }</pre>
     *
     * @param key   the key of the property
     * @param value the value of the property
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageChannelSettings addProperty(final @NotNull String key, final @Nullable Object value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Gets the group id for the consumer.
     *
     * @return the group id
     */
    public @NotNull String getGroupId() {
        return Objects.requireNonNull(
                properties.get(GROUP_ID),
                "group id has not been specified yet"
        ).toString();
    }

    /**
     * Defines the acknowledgement mode for sent messages.
     */
    @Getter
    @RequiredArgsConstructor
    public enum AcknowledgeMode {
        /**
         * No acknowledgement is required.
         */
        FIRE_AND_FORGET("0"),
        /**
         * One acknowledgement from the partition leader is required.
         */
        LEADER("1"),
        /**
         * All in-sync replicas must acknowledge the message.
         */
        ALL("all");

        private final @NotNull String value;

    }

    /**
     * Defines the compression type for sent messages.
     */
    public enum CompressionType {
        NONE,
        LZ4,
        SNAPPY,
        ZSTD,
        GZIP
    }

    /**
     * Defines where to start consuming the messages from.
     */
    public enum OffsetResetStrategy {
        /**
         * Consume from the first arrived message.
         */
        EARLIEST,
        /**
         * Consume from the latest arrived message (excluded).
         */
        LATEST
    }

}
