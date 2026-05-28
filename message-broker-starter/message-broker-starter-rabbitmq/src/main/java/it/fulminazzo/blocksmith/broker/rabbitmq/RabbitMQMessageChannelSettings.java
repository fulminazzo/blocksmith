package it.fulminazzo.blocksmith.broker.rabbitmq;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Message channel settings for RabbitMQ channels.
 *
 * @see RabbitMQMessageChannel
 * @see RabbitMQMessageBroker
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Getter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class RabbitMQMessageChannelSettings extends MessageChannelSettings<RabbitMQMessageChannelSettings> {
    private final @NotNull QueueSettings queueSettings = new QueueSettings();
    private boolean durable;

    /**
     * Sets the queue name.
     *
     * @param queueName the queue name
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageChannelSettings withQueueName(final @NotNull String queueName) {
        queueSettings.withQueueName(queueName);
        return this;
    }

    /**
     * Allows editing of the queue settings.
     *
     * @param queueSettings the function to edit the queue settings
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageChannelSettings withQueueSettings(
            final @NotNull Consumer<QueueSettings> queueSettings
    ) {
        queueSettings.accept(this.queueSettings);
        return this;
    }

    /**
     * Flags the exchange as <b>durable</b>.
     * A durable exchange will survive a broker restart.
     *
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageChannelSettings durable() {
        durable = true;
        return this;
    }

    /**
     * Settings for a RabbitMQ queue.
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class QueueSettings {
        private final @NotNull Map<String, Object> arguments = new HashMap<>();
        private @Nullable String queueName;
        private boolean durable;
        private boolean exclusive;
        private boolean autoDelete;

        /**
         * Sets the queue name.
         *
         * @param queueName the queue name
         * @return this object (for method chaining)
         */
        public @NotNull QueueSettings withQueueName(final @NotNull String queueName) {
            this.queueName = queueName;
            return this;
        }

        /**
         * Flags the queue as <b>durable</b>.
         * A durable queue will survive a broker restart.
         *
         * @return this object (for method chaining)
         */
        public @NotNull QueueSettings durable() {
            durable = true;
            return this;
        }

        /**
         * Flags the queue as <b>exclusive</b>.
         * An exclusive queue may only be accessed by the current connection.
         *
         * @return this object (for method chaining)
         */
        public @NotNull QueueSettings exclusive() {
            exclusive = true;
            return this;
        }

        /**
         * Flags the queue as <b>automatically deleted</b>.
         * The queue will be deleted when all consumers have finished using it.
         *
         * @return this object (for method chaining)
         */
        public @NotNull QueueSettings autoDelete() {
            autoDelete = true;
            return this;
        }

        /**
         * Adds a property for the queue.
         *
         * @param name  the name of the property
         * @param value the value of the property
         * @return this object (for method chaining)
         */
        public @NotNull QueueSettings addArgument(final @NotNull String name, final @Nullable Object value) {
            arguments.put(name, value);
            return this;
        }

        /**
         * Gets the queue name.
         *
         * @return the queue name
         */
        public @NotNull String getQueueName() {
            return Objects.requireNonNull(queueName, "queue name has not been specified yet");
        }

    }

}
