package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinator;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * Velocity implementation of {@link PluginMessageChannelCoordinatorFactory}.
 *
 * @see PluginMessageChannelCoordinatorFactory
 */
public final class VelocityPluginMessageChannelCoordinatorFactory implements PluginMessageChannelCoordinatorFactory {

    @Override
    public @NotNull PluginMessageChannelCoordinator create(final @NotNull PluginMessageRegistrar registrar) {
        return new VelocityPluginMessageChannelCoordinator(registrar);
    }

    @Override
    public @NotNull PluginMessageRegistrar createRegistrar(final @NotNull Object owner) {
        return new VelocityPluginMessageRegistrar(owner);
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return ProxyServer.class.isAssignableFrom(ownerType);
    }

}
