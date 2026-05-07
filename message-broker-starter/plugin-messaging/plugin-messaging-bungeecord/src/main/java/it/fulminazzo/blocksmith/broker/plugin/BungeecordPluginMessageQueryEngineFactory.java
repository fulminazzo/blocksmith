package it.fulminazzo.blocksmith.broker.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link PluginMessageQueryEngineFactory} for Bungeecord platforms.
 */
public final class BungeecordPluginMessageQueryEngineFactory implements PluginMessageQueryEngineFactory {

    @Override
    public @NotNull PluginMessageQueryEngine create(final @NotNull String channelName,
                                                    final @NotNull PluginMessageRegistrar registrar) {
        return new BungeecordPluginMessageQueryEngine(channelName, registrar);
    }

}
