package it.fulminazzo.blocksmith.broker.plugin;

import org.jetbrains.annotations.NotNull;

public class MockPluginMessageQueryEngineFactory implements PluginMessageQueryEngineFactory {

    @Override
    public @NotNull PluginMessageQueryEngine create(final @NotNull String channelName,
                                                    final @NotNull PluginMessageRegistrar registrar) {
        return new MockPluginMessageQueryEngine(channelName, registrar);
    }

}
