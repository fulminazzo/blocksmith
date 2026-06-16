package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactories;
import it.fulminazzo.blocksmith.broker.kafka.KafkaMessageBroker;
import it.fulminazzo.blocksmith.broker.kafka.KafkaMessageChannelSettings;
import it.fulminazzo.blocksmith.broker.memory.MemoryMessageBroker;
import it.fulminazzo.blocksmith.broker.memory.MemoryMessageChannelSettings;
import it.fulminazzo.blocksmith.broker.plugin.PluginMessageBroker;
import it.fulminazzo.blocksmith.broker.plugin.PluginMessageChannelSettings;
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageBroker;
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageChannelSettings;
import it.fulminazzo.blocksmith.broker.redis.RedisMessageBroker;
import it.fulminazzo.blocksmith.broker.redis.RedisMessageChannelSettings;
import it.fulminazzo.blocksmith.broker.tcp.TcpMessageBroker;
import it.fulminazzo.blocksmith.broker.tcp.TcpMessageChannelSettings;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * This is a special class encapsulating all {@link MessageChannelSettings} implementations.
 * The correct settings are then picked up according to the provided {@link MessageBroker}.
 * <br>
 * This ensures a Write-Once-Run-Everywhere approach and gives the developer the possibility to
 * integrate with any message broker provided.
 * <br>
 * Imagine this scenario: the developer loads a {@link MessageBrokerConfig} provided from the user
 * (via a configuration file). It is impossible to know prior the type of {@link MessageBroker}
 * that will be created (unless only one {@code message-broker-starter} module is being used).
 * Therefore, this class provides support for all possible combinations and implementations,
 * so that the developer does not have to worry about the underlying message broker and can deal with the
 * {@code message-broker-starter} API with ease.
 * <br>
 * Example:
 * <pre>{@code
 * final MemoryMessageChannelSettings memoryMessageChannelSettings = ...;
 * final TcpMessageChannelSettings tcpMessageChannelSettings = ...;
 * final RabbitMQMessageChannelSettings rabbitMQMessageChannelSettings = ...;
 * final RedisMessageChannelSettings redisMessageChannelSettings = ...;
 * final KafkaMessageChannelSettings kafkaMessageChannelSettings = ...;
 * final PluginMessageChannelSettings pluginMessageChannelSettings = ...;
 *
 * final AllMessageChannelSettings allMessageChannelSettings = AllMessageChannelSettings.builder()
 *         .memory(memoryMessageChannelSettings)
 *         .tcp(tcpMessageChannelSettings)
 *         .rabbitMQ(rabbitMQMessageChannelSettings)
 *         .redis(redisMessageChannelSettings)
 *         .kafka(kafkaMessageChannelSettings)
 *         .plugin(pluginMessageChannelSettings)
 *         .build();
 *
 * MessageBrokerConfig<?> messageBrokerConfig = ...; // loaded from a configuration provider
 * MessageBroker<MessageChannelSettings> messageBroker = MessageBrokerFactories.build(messageBrokerConfig);
 *
 * // We don't care what the message channel settings type are, they are fetched automatically
 * MessageChannel messageChannel = messageBroker.newChannel(
 *         allMessageChannelSettings.getChannelSettings(messageBroker)
 * );
 * }</pre>
 *
 * @see MessageChannelSettings
 * @see MessageBroker
 * @see MessageBrokerConfig
 * @see MessageBrokerFactories
 */
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public final class AllMessageChannelSettings {

    @NotNull MemoryMessageChannelSettings memory;

    @NotNull TcpMessageChannelSettings tcp;

    @NotNull RabbitMQMessageChannelSettings rabbitMQ;

    @NotNull RedisMessageChannelSettings redis;

    @NotNull KafkaMessageChannelSettings kafka;

    @NotNull PluginMessageChannelSettings plugin;

    /**
     * Converts the current settings to a {@link MessageChannelSettings}, according to the given Message broker.
     *
     * @param messageBroker the message broker
     * @return the message channel settings
     */
    public @NotNull MessageChannelSettings getMessageChannelSettings(final @NotNull MessageBroker<?> messageBroker) {
        if (messageBroker instanceof MemoryMessageBroker) return memory;
        else if (messageBroker instanceof TcpMessageBroker) return tcp;
        else if (messageBroker instanceof RabbitMQMessageBroker) return rabbitMQ;
        else if (messageBroker instanceof RedisMessageBroker) return redis;
        else if (messageBroker instanceof KafkaMessageBroker) return kafka;
        else if (messageBroker instanceof PluginMessageBroker) return plugin;
        else throw new IllegalArgumentException("Unsupported message broker type: " +
                    messageBroker.getClass().getCanonicalName());
    }

}
