package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

/**
 * Bukkit implementation of {@link PluginMessageRegistrar}.
 *
 * @see PluginMessageRegistrar
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
final class VelocityPluginMessageRegistrar implements PluginMessageRegistrar {
    private final @NotNull Object owner;

    @Override
    public @NotNull <P> P plugin() {
        return (P) owner;
    }

    @Override
    public @NotNull <S> S server() {
        return (S) getServer(owner);
    }

    private @NotNull ProxyServer getServer(final @NotNull Object plugin) {
        try {
            return Reflect.on(plugin)
                    .get(f -> !Modifier.isStatic(f.getModifiers()) && ProxyServer.class.isAssignableFrom(f.getType()))
                    .get();
        } catch (ReflectException e) {
            throw new ReflectException("Could not find %s field in %s", ProxyServer.class, plugin.getClass());
        }
    }

}
