package it.fulminazzo.blocksmith.broker.plugin.coordinator.bungee;

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinator;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bungeecord implementation of {@link PluginMessageChannelCoordinatorFactory}.
 *
 * @see PluginMessageChannelCoordinatorFactory
 */
public final class BungeecordPluginMessageChannelCoordinatorFactory implements PluginMessageChannelCoordinatorFactory {

    @Override
    public @NotNull PluginMessageChannelCoordinator create(final @NotNull PluginMessageRegistrar registrar) {
        return new BungeecordPluginMessageChannelCoordinator(registrar);
    }

    @Override
    public boolean supportsRegistrar(final @NotNull PluginMessageRegistrar registrar) {
        return Plugin.class.isAssignableFrom(registrar.plugin().getClass());
    }

}
