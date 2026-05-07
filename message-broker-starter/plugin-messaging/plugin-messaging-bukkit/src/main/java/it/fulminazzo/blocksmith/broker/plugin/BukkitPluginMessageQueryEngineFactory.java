package it.fulminazzo.blocksmith.broker.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link PluginMessageQueryEngineFactory} for Bukkit platforms.
 */
public final class BukkitPluginMessageQueryEngineFactory implements PluginMessageQueryEngineFactory {

    @Override
    public @NotNull PluginMessageQueryEngine create(final @NotNull String channelName,
                                                    final @NotNull PluginMessageRegistrar registrar) {
        return new BukkitPluginMessageQueryEngine(channelName, registrar);
    }

}
