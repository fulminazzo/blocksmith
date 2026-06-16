package it.fulminazzo.blocksmith.broker.kafka.config;

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactories;
import it.fulminazzo.blocksmith.broker.kafka.KafkaMessageBroker;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.PositiveOrZero;
import it.fulminazzo.blocksmith.validation.annotation.Size;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.common.security.auth.SecurityProtocol;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link MessageBrokerConfig} for {@link KafkaMessageBroker}.
 *
 * @see MessageBrokerConfig
 * @see KafkaMessageBroker
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public final class KafkaMessageBrokerConfig extends MessageBrokerConfig<KafkaMessageBrokerConfig> {

    static {
        MessageBrokerFactories.registerFactory(
                KafkaMessageBrokerConfig.class,
                new KafkaMessageBrokerFactory()
        );
    }

    @NonNull(exceptionMessage = "'bootstrap servers' must be declared")
    @Size(min = 1, max = Integer.MAX_VALUE, exceptionMessage = "'bootstrapServers' must at least have one server configured")
    Set<BootstrapServerConfig> bootstrapServers = new HashSet<>();

    @NonNull(exceptionMessage = "'security protocol' must be declared")
    SecurityProtocol securityProtocol = SecurityProtocol.PLAINTEXT;

    @NonNull(exceptionMessage = "'client DNS lookup' must be declared")
    ClientDnsLookup clientDnsLookup = ClientDnsLookup.USE_ALL_DNS_IPS;

    @PositiveOrZero
    @NonNull(exceptionMessage = "'reconnect backoff' must be declared")
    Integer reconnectBackoff = 50;

    @PositiveOrZero
    @NonNull(exceptionMessage = "'reconnect backoff max' must be declared")
    Integer reconnectBackoffMax = 1_000;

    @PositiveOrZero
    @NonNull(exceptionMessage = "'request timeout' must be declared")
    Integer requestTimeout = 30_000;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Accessors(chain = true)
    public static final class BootstrapServerConfig {

        @NonNull(exceptionMessage = "'host' must be declared")
        String host;

        @NonNull(exceptionMessage = "'port' must be declared")
        Integer port;

    }

}
