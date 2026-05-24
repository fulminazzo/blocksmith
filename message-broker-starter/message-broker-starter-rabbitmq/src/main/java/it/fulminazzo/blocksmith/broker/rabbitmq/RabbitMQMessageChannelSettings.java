package it.fulminazzo.blocksmith.broker.rabbitmq;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Message channel settings for RabbitMQ databases.
 *
 * @see RabbitMQMessageChannel
 * @see RabbitMQMessageBroker
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class RabbitMQMessageChannelSettings extends MessageChannelSettings<RabbitMQMessageChannelSettings> {
    private @Nullable String queueName;

    /**
     * Sets the queue name.
     *
     * @param queueName the queue name
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageChannelSettings withQueueName(@NotNull String queueName) {
        this.queueName = queueName;
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
