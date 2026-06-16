package it.fulminazzo.blocksmith.broker.config;

import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.broker.MessageBroker;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * The supported types of message broker configurations.
 *
 * @see MessageBrokerConfig
 * @see MessageBrokerFactory
 * @see MessageBroker
 */
public enum MessageBrokerType {
    /**
     * Identifies the in-memory message broker.
     */
    MEMORY,
    /**
     * Identifies the TCP message broker.
     */
    TCP,
    /**
     * Identifies the RabbitMQ message broker.
     */
    RABBITMQ,
    /**
     * Identifies the Redis message broker.
     */
    REDIS,
    /**
     * Identifies the Kafka message broker.
     */
    KAFKA,
    /**
     * Identifies the Plugin message broker.
     */
    PLUGIN;

    /**
     * Gets the {@link MessageBrokerConfig} configuration class for this type.
     *
     * @return the message broker config
     */
    @SuppressWarnings("unchecked")
    public @NotNull Class<MessageBrokerConfig<?>> getConfigClass() {
        String type = name().toLowerCase(Locale.ROOT);
        type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
        if (this == RABBITMQ) type = "RabbitMQ";

        String lowercaseType = type.toLowerCase(Locale.ROOT);

        String className = MessageBrokerConfig.class.getCanonicalName()
                .replace("config.", lowercaseType + ".config." + type);
        try {
            return (Class<MessageBrokerConfig<?>>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            String moduleName = String.format("%s.%s:%s-%s",
                    ProjectInfo.GROUP,
                    ProjectInfo.PROJECT_NAME,
                    ProjectInfo.MODULE_NAME.replace("-base", ""),
                    lowercaseType
            );
            throw new IllegalStateException(
                    String.format("Could not find suitable %s for %s. ", MessageBrokerConfig.class.getSimpleName(), type)
                            + String.format("Please check that the module %s is correctly installed.", moduleName)
            );
        }
    }

}
