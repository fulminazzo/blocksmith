package it.fulminazzo.blocksmith.broker.tcp.config;

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactories;
import it.fulminazzo.blocksmith.broker.tcp.TcpMessageBroker;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.Port;
import it.fulminazzo.blocksmith.validation.annotation.PositiveOrZero;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * {@link TcpMessageBrokerConfig} for {@link TcpMessageBroker}.
 *
 * @see MessageBrokerConfig
 * @see TcpMessageBroker
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public final class TcpMessageBrokerConfig extends MessageBrokerConfig<TcpMessageBrokerConfig> {

    static {
        MessageBrokerFactories.registerFactory(
                TcpMessageBrokerConfig.class,
                new TcpMessageBrokerFactory()
        );
    }

    @Port
    @NonNull(exceptionMessage = "'port' must be declared")
    Integer port = 30926;

    @PositiveOrZero
    @NonNull(exceptionMessage = "'retry interval' must be declared")
    Long retryInterval = 1_000L;

}
