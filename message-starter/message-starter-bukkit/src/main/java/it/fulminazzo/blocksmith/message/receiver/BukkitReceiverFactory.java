package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.ServerApplication;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ReceiverFactory} for Bukkit platform.
 */
public final class BukkitReceiverFactory extends AbstractReceiverFactory {
    private BukkitAudiences adventure;

    @Override
    public @NotNull ReceiverFactory setup(final @NotNull ServerApplication application) {
        super.setup(application);
        adventure = BukkitAudiences.create(application.as(Plugin.class));
        return this;
    }

    @Override
    protected @NotNull Collection<Receiver> getAllReceiversImpl() {
        final Server server = Bukkit.getServer();
        return Stream.concat(
                server.getOnlinePlayers().stream(),
                Stream.of(server.getConsoleSender())
        ).map(this::create).collect(Collectors.toList());
    }

    @Override
    protected <R> @NotNull Receiver createImpl(final @NotNull R receiver) {
        return new BukkitReceiver(adventure, (CommandSender) receiver);
    }

    @Override
    protected boolean supportsImpl(final @NotNull Class<?> receiverType) {
        return CommandSender.class.isAssignableFrom(receiverType);
    }

}
