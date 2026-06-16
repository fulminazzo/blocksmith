package it.fulminazzo.blocksmith.broker.plugin.config;

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactories;
import it.fulminazzo.blocksmith.broker.plugin.PluginMessageBroker;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * {@link MessageBrokerConfig} for {@link PluginMessageBroker}.
 *
 * @see MessageBrokerConfig
 * @see PluginMessageBroker
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public final class PluginMessageBrokerConfig extends MessageBrokerConfig<PluginMessageBrokerConfig> {

    static {
        MessageBrokerFactories.registerFactory(
                PluginMessageBrokerConfig.class,
                new PluginMessageBrokerFactory()
        );
    }

    Object owner;

}
