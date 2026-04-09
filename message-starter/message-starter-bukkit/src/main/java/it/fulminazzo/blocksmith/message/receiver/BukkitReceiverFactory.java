package it.fulminazzo.blocksmith.message.receiver;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ReceiverFactory} for Bukkit platform.
 */
public final class BukkitReceiverFactory extends AbstractReceiverFactory {
    private static @Nullable BukkitAudiences adventure;

    @Override
    protected @NotNull Collection<Receiver> getAllReceiversImpl() {
        final Server server = Bukkit.getServer();
        return Stream.concat(
                server.getOnlinePlayers().stream(),
                Stream.of(server.getConsoleSender())
        ).map(this::create).collect(Collectors.toList());
    }

    @Override
    protected @NotNull <R> Receiver createImpl(final @NotNull R receiver) {
        return new BukkitReceiver(getAdventure(), (CommandSender) receiver);
    }

    @Override
    protected boolean supportsImpl(final @NotNull Class<?> receiverType) {
        return CommandSender.class.isAssignableFrom(receiverType);
    }

    /**
     * Gets the Text Adventure converter for command senders.
     *
     * @return the converter
     */
    static @NotNull BukkitAudiences getAdventure() {
        if (adventure == null)
            adventure = BukkitAudiences.create(JavaPlugin.getProvidingPlugin(BukkitReceiver.class));
        return adventure;
    }

}
