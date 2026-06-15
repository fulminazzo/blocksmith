package it.fulminazzo.blocksmith.broker.plugin;

import it.fulminazzo.blocksmith.broker.AbstractMessageChannel;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Implementation of {@link it.fulminazzo.blocksmith.broker.MessageChannel} for the Minecraft plugin messaging system.
 *
 * @see PluginMessageChannelSettings
 * @see PluginMessageQueryEngine
 */
public class PluginMessageChannel extends AbstractMessageChannel<PluginMessageQueryEngine> {

    /**
     * Instantiates a new Plugin message channel.
     *
     * @param queryEngine the query engine
     * @param mapper      the mapper
     */
    protected PluginMessageChannel(
            final @NonNull PluginMessageQueryEngine queryEngine,
            final @NotNull Mapper mapper
    ) {
        super(queryEngine, mapper);
    }

}
