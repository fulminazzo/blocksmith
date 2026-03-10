package it.fulminazzo.blocksmith.message.receiver;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link ReceiverFactory} for Bukkit platform.
 */
public final class BukkitReceiverFactory implements ReceiverFactory {
    private static @Nullable BukkitAudiences adventure;

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        return new BukkitReceiver(getAdventure(), (CommandSender) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
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
