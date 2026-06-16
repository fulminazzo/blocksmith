package it.fulminazzo.blocksmith.broker.rabbitmq.config;

import com.rabbitmq.client.ConnectionFactory;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactories;
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageBroker;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.Port;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Range;

/**
 * {@link MessageBrokerConfig} for {@link RabbitMQMessageBroker}.
 *
 * @see MessageBrokerConfig
 * @see RabbitMQMessageBroker
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public final class RabbitMQMessageBrokerConfig extends MessageBrokerConfig<RabbitMQMessageBrokerConfig> {

    static {
        MessageBrokerFactories.registerFactory(
                RabbitMQMessageBrokerConfig.class,
                new RabbitMQMessageBrokerFactory()
        );
    }

    @NonNull(exceptionMessage = "'host' must be declared")
    String host;

    @Port
    @Range(from = 1, to = 65535)
    @NonNull(exceptionMessage = "'port' must be declared")
    Integer port;

    @NonNull(exceptionMessage = "'username' must be declared")
    String username = ConnectionFactory.DEFAULT_USER;

    @NonNull(exceptionMessage = "'password' must be declared")
    String password = ConnectionFactory.DEFAULT_PASS;

}
