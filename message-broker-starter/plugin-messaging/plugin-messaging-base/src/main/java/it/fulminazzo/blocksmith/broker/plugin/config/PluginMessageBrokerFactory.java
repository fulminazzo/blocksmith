package it.fulminazzo.blocksmith.broker.plugin.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory;
import it.fulminazzo.blocksmith.broker.plugin.PluginMessageBroker;
import org.jetbrains.annotations.NotNull;

final class PluginMessageBrokerFactory implements MessageBrokerFactory {

    @Override
    public @NotNull MessageBroker<?> build(final @NotNull MessageBrokerConfig<?> config) {
        PluginMessageBrokerConfig mbConfig = (PluginMessageBrokerConfig) config;
        return PluginMessageBroker.builder()
                .owner(mbConfig.getOwner())
                .mapper(mbConfig.getMapper())
                .build();
    }

}
