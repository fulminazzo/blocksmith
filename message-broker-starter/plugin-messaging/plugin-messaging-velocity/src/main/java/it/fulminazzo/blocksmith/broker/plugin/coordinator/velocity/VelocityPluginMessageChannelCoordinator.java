package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Velocity implementation of {@link PluginMessageChannelCoordinator}.
 * <br>
 * Uses the name of the servers as node IDs.
 *
 * @see PluginMessageChannelCoordinator
 * @see ProxyPluginMessageChannelCoordinator
 */
public final class VelocityPluginMessageChannelCoordinator
        extends ProxyPluginMessageChannelCoordinator<String> {
    private final @NotNull ProxyServer server;

    /**
     * Instantiates a new Velocity plugin message channel coordinator.
     *
     * @param registrar the registrar
     */
    VelocityPluginMessageChannelCoordinator(final @NotNull PluginMessageRegistrar registrar) {
        super(registrar);
        this.server = this.registrar.server();
        server.getEventManager().register(registrar.plugin(), this);
    }

    /**
     * Attempts to resend the failed messages whenever a player joins a node
     * (will only have an effect if no other player was online at the time of publishing).
     *
     * @param event the event
     */
    @Subscribe
    public void on(final @NotNull ServerConnectedEvent event) {
        getNodePublisher(event.getServer().getServerInfo().getName()).republishFailedMessages();
    }

    /**
     * Handles incoming plugin messages.
     *
     * @param event the event
     */
    @Subscribe
    public void on(final @NotNull PluginMessageEvent event) {
        handleIncomingMessage(
                VelocityChannelUtils.toChannelName(event.getIdentifier()),
                event.getData()
        );
    }

    @Override
    public void close() {
        super.close();
        server.getEventManager().unregisterListener(registrar.plugin(), this);
    }

    @Override
    protected @NotNull PluginMessagePublisher newNodePublisher(final @NotNull String node) {
        return new VelocityPluginMessagePublisher(node);
    }

    @Override
    protected void registerChannel(final @NotNull String channelName) {
        server.getChannelRegistrar().register(VelocityChannelUtils.toIdentifier(channelName));
    }

    @Override
    protected void unregisterChannel(final @NotNull String channelName) {
        server.getChannelRegistrar().unregister(VelocityChannelUtils.toIdentifier(channelName));
    }

    @Override
    protected @NotNull Collection<String> getAllNodes() {
        return server.getAllServers().stream()
                .map(RegisteredServer::getServerInfo)
                .map(ServerInfo::getName)
                .collect(Collectors.toList());
    }

    /**
     * Velocity implementation of {@link PluginMessagePublisher}.
     * <br>
     * Uses the name of the server as the node ID.
     *
     * @see PluginMessagePublisher
     */
    @RequiredArgsConstructor
    private final class VelocityPluginMessagePublisher extends AbstractPluginMessagePublisher {
        private final @NotNull String serverId;

        /**
         * Gets the node.
         *
         * @return the node
         */
        private @NotNull RegisteredServer getNode() {
            return server.getServer(serverId)
                    .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        }

        @Override
        protected boolean publishImpl(final @NotNull String channelName, final byte @NotNull [] message) {
            return getNode().sendPluginMessage(VelocityChannelUtils.toIdentifier(channelName), message);
        }

    }

}
