package it.fulminazzo.blocksmith.broker.plugin.coordinator.bukkit;

import it.fulminazzo.blocksmith.broker.plugin.coordinator.AbstractPluginMessagePublisher;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinator;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessagePublisher;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar;
import lombok.experimental.Delegate;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Bukkit implementation of {@link PluginMessageChannelCoordinator}.
 *
 * @see PluginMessageChannelCoordinator
 */
public final class BukkitPluginMessageChannelCoordinator
        extends PluginMessageChannelCoordinator
        implements PluginMessageListener, Listener {
    private final @NotNull Server server;
    @Delegate
    private final @NotNull PluginMessagePublisher publisher;

    /**
     * Instantiates a new Bukkit plugin message channel coordinator.
     *
     * @param registrar the registrar
     */
    BukkitPluginMessageChannelCoordinator(final @NotNull PluginMessageRegistrar registrar) {
        super(registrar);
        this.server = this.registrar.server();
        this.publisher = new BukkitPluginMessageChannelPublisher();
        server.getPluginManager().registerEvents(this, this.registrar.plugin());
    }

    /**
     * Attempts to resend the failed messages whenever a player joins
     * (will only have an effect if no other player was online at the time of publishing).
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on(final @NotNull PlayerJoinEvent event) {
        publisher.republishFailedMessages();
    }

    @Override
    public void onPluginMessageReceived(
            final @NotNull String channel,
            final @NotNull Player player,
            final byte @NotNull [] message
    ) {
        handleIncomingMessage(channel, message);
    }

    @Override
    public void close() {
        super.close();
        HandlerList.unregisterAll(this);
    }

    @Override
    protected void registerChannel(final @NotNull String channelName) {
        Plugin plugin = registrar.plugin();
        server.getMessenger().registerIncomingPluginChannel(
                plugin,
                channelName,
                this
        );
        server.getMessenger().registerOutgoingPluginChannel(plugin, channelName);
    }

    @Override
    protected void unregisterChannel(final @NotNull String channelName) {
        Plugin plugin = registrar.plugin();
        server.getMessenger().unregisterIncomingPluginChannel(plugin, channelName);
        server.getMessenger().unregisterOutgoingPluginChannel(plugin, channelName);
    }

    /**
     * Bukkit implementation of {@link PluginMessagePublisher}.
     *
     * @see PluginMessagePublisher
     */
    private final class BukkitPluginMessageChannelPublisher extends AbstractPluginMessagePublisher {

        @Override
        protected boolean publishImpl(final @NotNull String channelName, final byte @NotNull [] message) {
            Player player = server.getOnlinePlayers().stream()
                    /*
                     * We are sorting for OP players first, so that if an OP player is present,
                     * they will be chosen as bridge for the proxy, in an attempt to limit
                     * malicious actors intercepting messages.
                     */
                    .sorted(Comparator.comparing(p -> !p.isOp()))
                    .findAny().orElse(null);
            if (player != null) {
                player.sendPluginMessage(registrar.plugin(), channelName, message);
                return true;
            } else return false;
        }

    }

}
