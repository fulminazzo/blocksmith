package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.ServerApplication;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ReceiverFactory} for BungeeCord platform.
 */
public final class BungeeReceiverFactory extends AbstractReceiverFactory {
    private BungeeAudiences adventure;

    @Override
    public @NotNull ReceiverFactory setup(final @NotNull ServerApplication application) {
        super.setup(application);
        adventure = BungeeAudiences.create(application.as(Plugin.class));
        return this;
    }

    @Override
    protected @NotNull Collection<Receiver> getAllReceiversImpl() {
        final ProxyServer server = ProxyServer.getInstance();
        return Stream.concat(
                server.getPlayers().stream(),
                Stream.of(server.getConsole())
        ).map(this::create).collect(Collectors.toList());
    }

    @Override
    protected <R> @NotNull Receiver createImpl(final @NotNull R receiver) {
        return new BungeeReceiver(adventure, (CommandSender) receiver);
    }

    @Override
    protected boolean supportsImpl(final @NotNull Class<?> receiverType) {
        return CommandSender.class.isAssignableFrom(receiverType);
    }

}
