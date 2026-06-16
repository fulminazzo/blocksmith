package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Mock implementation of {@link PluginMessageChannelCoordinator}.
 */
public final class MockPluginMessageChannelCoordinator extends PluginMessageChannelCoordinator {
    private static final @NotNull Map<
            String,
            Set<MockPluginMessageChannelCoordinator>
            > COORDINATORS = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Mock plugin message channel coordinator.
     */
    public MockPluginMessageChannelCoordinator() {
        super(new PluginMessageRegistrar() {

            @Override
            public @NotNull <P> P plugin() {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull <S> S server() {
                throw new UnsupportedOperationException();
            }

        });
    }

    @Override
    public boolean publish(final @NotNull String channelName, final byte @NotNull [] message) {
        getChannelCoordinators(channelName).forEach(c ->
                c.handleIncomingMessage(channelName, message)
        );
        return true;
    }

    @Override
    public void republishFailedMessages() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void registerChannel(final @NotNull String channelName) {
        getChannelCoordinators(channelName).add(this);
    }

    @Override
    protected void unregisterChannel(final @NotNull String channelName) {
        getChannelCoordinators(channelName).remove(this);
    }

    private static Set<MockPluginMessageChannelCoordinator> getChannelCoordinators(final @NotNull String channelName) {
        return COORDINATORS.computeIfAbsent(channelName, _ -> new CopyOnWriteArraySet<>());
    }

}
