package it.fulminazzo.blocksmith.broker.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects all the {@link MessageBrokerFactory} implementations.
 *
 * @see MessageBrokerConfig
 * @see MessageBrokerFactory
 * @see MessageBroker
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageBrokerFactories {
    private static final @NotNull Map<
            Class<? extends MessageBrokerConfig<?>>,
            MessageBrokerFactory
            > FACTORIES = new ConcurrentHashMap<>();

    /**
     * Instantiates a new {@link MessageBroker} from the given configuration.
     *
     * @param messageBrokerConfig the message broker configuration
     * @return the message broker
     */
    @SuppressWarnings("unchecked")
    public static @NotNull MessageBroker<MessageChannelSettings> build(
            final @NotNull MessageBrokerConfig<?> messageBrokerConfig
    ) {
        MessageBrokerFactory messageBrokerFactory = FACTORIES.get(messageBrokerConfig.getClass());
        if (messageBrokerFactory == null)
            throw new IllegalArgumentException(String.format(
                    "No %s factory currently registered for configuration type: %s",
                    MessageBroker.class.getSimpleName(),
                    messageBrokerConfig.getClass().getCanonicalName()
            ));
        else return (MessageBroker<MessageChannelSettings>) messageBrokerFactory.build(messageBrokerConfig);
    }

    /**
     * Registers a new factory for the given {@link MessageBrokerConfig} type.
     *
     * @param configClass the config class
     * @param factory     the factory
     */
    public static void registerFactory(
            final @NotNull Class<? extends MessageBrokerConfig<?>> configClass,
            final @NotNull MessageBrokerFactory factory
    ) {
        FACTORIES.put(configClass, factory);
    }

}
