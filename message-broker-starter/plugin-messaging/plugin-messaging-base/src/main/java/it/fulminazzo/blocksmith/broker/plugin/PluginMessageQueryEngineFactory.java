package it.fulminazzo.blocksmith.broker.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * A factory for {@link PluginMessageQueryEngine} instances.
 */
public interface PluginMessageQueryEngineFactory {

    /**
     * Instantiates a new Plugin message query engine.
     *
     * @param channelName the channel name.
     *                    <b>NOTE</b>: implementations will <b>prepend</b> the registrar
     *                    name automatically
     * @param registrar   the registrar for the internal registration of channels
     * @return the plugin message query engine
     */
    @NotNull PluginMessageQueryEngine create(final @NotNull String channelName,
                                             final @NotNull PluginMessageRegistrar registrar);

}
