package it.fulminazzo.blocksmith.message.receiver;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link ReceiverFactory} for Bukkit platform.
 */
public final class BukkitReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        return new BukkitReceiver((CommandSender) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return CommandSender.class.isAssignableFrom(receiverType);
    }

}
