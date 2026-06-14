package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A special {@link PluginMessageChannelCoordinator} prepared for proxies configurations.
 * Contains an internal map for handling all the nodes of a proxy as instances of {@link PluginMessagePublisher},
 * so that each node can handle failed publications accordingly.
 *
 * @param <N> the type of the nodes (IDs are preferred for memory efficiency)
 * @see PluginMessageChannelCoordinator
 */
public abstract class ProxyPluginMessageChannelCoordinator<N> extends PluginMessageChannelCoordinator {
    private final @NotNull Map<N, PluginMessagePublisher> nodes = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Proxy plugin message channel coordinator.
     *
     * @param registrar the registrar
     */
    protected ProxyPluginMessageChannelCoordinator(final @NotNull PluginMessageRegistrar registrar) {
        super(registrar);
    }

    /**
     * Instantiates a new publisher for the given node.
     *
     * @param node the node
     * @return the publisher
     */
    protected abstract @NotNull PluginMessagePublisher newNodePublisher(final @NotNull N node);

    /**
     * Fetches all the currently connected nodes.
     *
     * @return the nodes
     */
    protected abstract @NotNull Collection<N> getAllNodes();

    /**
     * Gets the publisher for the given node.
     *
     * @param node the node
     * @return the publisher
     */
    protected @NotNull PluginMessagePublisher getNodePublisher(final @NotNull N node) {
        return nodes.computeIfAbsent(node, this::newNodePublisher);
    }

    private void refreshNodes() {
        @NotNull Collection<N> currNodes = getAllNodes();
        nodes.keySet().removeIf(n -> !currNodes.contains(n));
        currNodes.forEach(n -> nodes.put(n, newNodePublisher(n)));
    }

    @Override
    public boolean publish(final @NotNull String channelName, final byte @NotNull [] message) {
        refreshNodes();
        return nodes.values().stream().anyMatch(n -> n.publish(channelName, message));
    }

    @Override
    public void republishFailedMessages() {
        refreshNodes();
        nodes.values().forEach(PluginMessagePublisher::republishFailedMessages);
    }

}
