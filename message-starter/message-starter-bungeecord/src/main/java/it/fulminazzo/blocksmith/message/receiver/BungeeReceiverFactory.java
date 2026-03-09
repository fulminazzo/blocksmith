package it.fulminazzo.blocksmith.message.receiver;

import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link ReceiverFactory} for BungeeCord platform.
 */
public final class BungeeReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        return new BungeeReceiver((CommandSender) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return CommandSender.class.isAssignableFrom(receiverType);
    }

}
