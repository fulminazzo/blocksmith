package it.fulminazzo.blocksmith.broker.plugin.coordinator.bukkit;

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit implementation of {@link PluginMessageRegistrar}.
 *
 * @see PluginMessageRegistrar
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
final class BukkitPluginMessageRegistrar implements PluginMessageRegistrar {
    private final @NotNull Plugin owner;

    @Override
    public @NotNull <P> P plugin() {
        return (P) owner;
    }

    @Override
    public @NotNull <S> S server() {
        return (S) owner.getServer();
    }

}
