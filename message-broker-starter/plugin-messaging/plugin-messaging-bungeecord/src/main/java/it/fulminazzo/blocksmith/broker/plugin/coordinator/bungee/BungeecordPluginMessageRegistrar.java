package it.fulminazzo.blocksmith.broker.plugin.coordinator.bungee;

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bungeecord implementation of {@link PluginMessageRegistrar}.
 *
 * @see PluginMessageRegistrar
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
final class BungeecordPluginMessageRegistrar implements PluginMessageRegistrar {
    private final @NotNull Plugin owner;

    @Override
    public @NotNull <P> P plugin() {
        return (P) owner;
    }

    @Override
    public @NotNull <S> S server() {
        return (S) owner.getProxy();
    }

}
