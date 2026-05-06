package it.fulminazzo.blocksmith.broker.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class MockPluginMessageQueryEngine extends PluginMessageQueryEngine {
    private static final @NotNull List<MockPluginMessageQueryEngine> REGISTERED = new ArrayList<>();

    /**
     * Instantiates a new Mock plugin message query engine.
     *
     * @param channelName the channel name.
     *                    <b>NOTE</b>: implementations will <b>prepend</b> the {@link #registrar}
     *                    name automatically
     * @param registrar   the registrar for the internal registration of channels
     */
    public MockPluginMessageQueryEngine(final @NotNull String channelName, final @NotNull PluginMessageRegistrar registrar) {
        super(channelName, registrar);
        REGISTERED.add(this);
    }

    @Override
    protected boolean publish(final byte @NotNull [] payload) {
        REGISTERED.stream()
                .filter(l -> !l.equals(this))
                .forEach(l -> l.handleMessage(payload));
        return true;
    }

}
