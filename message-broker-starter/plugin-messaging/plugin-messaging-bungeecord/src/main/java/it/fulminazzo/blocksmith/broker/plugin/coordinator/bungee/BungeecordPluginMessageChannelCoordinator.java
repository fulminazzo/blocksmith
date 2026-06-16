package it.fulminazzo.blocksmith.broker.plugin.coordinator.bungee;

import it.fulminazzo.blocksmith.broker.plugin.coordinator.*;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Bungeecord implementation of {@link PluginMessageChannelCoordinator}.
 * <br>
 * Uses the name of the servers as node IDs.
 *
 * @see PluginMessageChannelCoordinator
 * @see ProxyPluginMessageChannelCoordinator
 */
final class BungeecordPluginMessageChannelCoordinator
        extends ProxyPluginMessageChannelCoordinator<String>
        implements Listener {
    private final @NotNull ProxyServer server;

    /**
     * Instantiates a new Bungeecord plugin message channel coordinator.
     *
     * @param registrar the registrar
     */
    BungeecordPluginMessageChannelCoordinator(final @NotNull PluginMessageRegistrar registrar) {
        super(registrar);
        this.server = this.registrar.server();
        server.getPluginManager().registerListener(this.registrar.plugin(), this);
    }

    /**
     * Attempts to resend the failed messages whenever a player joins a node
     * (will only have an effect if no other player was online at the time of publishing).
     *
     * @param event the event
     */
    @EventHandler
    public void on(final @NotNull ServerConnectedEvent event) {
        getNodePublisher(event.getServer().getInfo().getName()).republishFailedMessages();
    }

    /**
     * Handles incoming plugin messages.
     *
     * @param event the event
     */
    @EventHandler
    public void on(final @NotNull PluginMessageEvent event) {
        handleIncomingMessage(event.getTag(), event.getData());
    }

    @Override
    public void close() {
        super.close();
        server.getPluginManager().unregisterListener(this);
    }

    @Override
    protected @NotNull PluginMessagePublisher newNodePublisher(final @NotNull String node) {
        return new BungeePluginMessagePublisher(node);
    }

    @Override
    protected void registerChannel(final @NotNull String channelName) {
        server.registerChannel(channelName);
    }

    @Override
    protected void unregisterChannel(final @NotNull String channelName) {
        server.unregisterChannel(channelName);
    }

    @Override
    protected @NotNull Collection<String> getAllNodes() {
        return server.getServers().keySet();
    }

    /**
     * Bungeecord implementation of {@link PluginMessagePublisher}.
     * <br>
     * Uses the name of the server as the node ID.
     *
     * @see PluginMessagePublisher
     */
    @RequiredArgsConstructor
    private final class BungeePluginMessagePublisher extends AbstractPluginMessagePublisher {
        private final @NotNull String serverId;

        /**
         * Gets the node.
         *
         * @return the node
         */
        private @NotNull ServerInfo getNode() {
            return server.getServerInfo(serverId);
        }

        @Override
        protected boolean publishImpl(final @NotNull String channelName, final byte @NotNull [] message) {
            return getNode().sendData(
                    channelName,
                    message,
                    false // leave handling of failures to our own implementation
            );
        }

    }

}
