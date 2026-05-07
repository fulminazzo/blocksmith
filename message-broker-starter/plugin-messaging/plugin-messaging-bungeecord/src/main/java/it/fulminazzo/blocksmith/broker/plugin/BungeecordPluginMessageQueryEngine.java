package it.fulminazzo.blocksmith.broker.plugin;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of {@link PluginMessageQueryEngine} for Bungeecord platforms.
 */
public final class BungeecordPluginMessageQueryEngine extends PluginMessageQueryEngine implements Listener {
    private final @NotNull ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Instantiates a new Bungeecord plugin message query engine.
     *
     * @param channelName the channel name.
     *                    <b>NOTE</b>: implementations will <b>prepend</b> the {@link #registrar}
     *                    name automatically
     * @param registrar   the registrar for the internal registration of channels
     */
    BungeecordPluginMessageQueryEngine(final @NotNull String channelName, final @NotNull PluginMessageRegistrar registrar) {
        super(channelName, registrar);
        ProxyServer server = registrar.server();
        server.getPluginManager().registerListener(registrar.as(Plugin.class), this);
        server.registerChannel(this.channelName);
    }

    @EventHandler
    public void on(final @NotNull PluginMessageEvent event) {
        if (event.getTag().equals(channelName))
            executor.execute(() -> handleMessage(event.getData()));
    }

    @Override
    protected boolean publish(final byte @NotNull [] payload) {
        ProxyServer server = registrar.server();
        server.getServers().values().forEach(s -> s.sendData(channelName, payload));
        return true;
    }

    @Override
    public void close() {
        super.close();
        executor.shutdown();
        ProxyServer server = registrar.server();
        server.unregisterChannel(channelName);
        server.getPluginManager().unregisterListener(this);
    }

}
