package it.fulminazzo.blocksmith.broker.plugin.coordinator.bukkit;

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinator;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit implementation of {@link PluginMessageChannelCoordinatorFactory}.
 *
 * @see PluginMessageChannelCoordinatorFactory
 */
public final class BukkitPluginMessageChannelCoordinatorFactory implements PluginMessageChannelCoordinatorFactory {

    @Override
    public @NotNull PluginMessageChannelCoordinator create(final @NotNull PluginMessageRegistrar registrar) {
        return new BukkitPluginMessageChannelCoordinator(registrar);
    }

    @Override
    public boolean supportsRegistrar(final @NotNull PluginMessageRegistrar registrar) {
        return Plugin.class.isAssignableFrom(registrar.plugin().getClass());
    }

}
