package it.fulminazzo.blocksmith.broker.plugin;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Implementation of {@link PluginMessageQueryEngine} for Bukkit platforms.
 */
public final class BukkitPluginMessageQueryEngine extends PluginMessageQueryEngine implements Listener, PluginMessageListener {

    /**
     * Instantiates a new Bukkit plugin message query engine.
     *
     * @param channelName the channel name.
     *                    <b>NOTE</b>: implementations will <b>prepend</b> the {@link #registrar}
     *                    name automatically
     * @param registrar   the registrar for the internal registration of channels
     */
    BukkitPluginMessageQueryEngine(final @NotNull String channelName, final @NotNull PluginMessageRegistrar registrar) {
        super(channelName, registrar);
        Server server = registrar.server();
        server.getPluginManager().registerEvents(this, getPlugin());
        Messenger messenger = server.getMessenger();
        messenger.registerIncomingPluginChannel(getPlugin(), this.channelName, this);
        messenger.registerOutgoingPluginChannel(getPlugin(), this.channelName);
    }

    @Override
    public void onPluginMessageReceived(final @NotNull String channel,
                                        final @NotNull Player player,
                                        final byte @NotNull [] message) {
        if (channel.equals(channelName)) handleMessage(message);
    }

    @EventHandler
    public void on(final @NotNull PlayerJoinEvent event) {
        resendPendingMessages();
    }

    @Override
    protected boolean publish(final byte @NotNull [] payload) {
        Player player = ((Server) registrar.server()).getOnlinePlayers().stream()
                /*
                 * We are sorting for OP players first, so that if an OP player is present,
                 * they will be chosen as bridge for the proxy, in an attempt to limit
                 * malicious actors intercepting messages.
                 */
                .sorted(Comparator.comparing(p -> !p.isOp()))
                .findAny().orElse(null);
        if (player != null) {
            player.sendPluginMessage(getPlugin(), channelName, payload);
            return true;
        } else return false;
    }

    @Override
    public void close() {
        super.close();
        Server server = registrar.server();
        HandlerList.unregisterAll(this);
        Messenger messenger = server.getMessenger();
        messenger.unregisterIncomingPluginChannel(getPlugin(), channelName);
        messenger.unregisterOutgoingPluginChannel(getPlugin(), channelName);
    }

    private @NotNull Plugin getPlugin() {
        return registrar.as(Plugin.class);
    }
}
