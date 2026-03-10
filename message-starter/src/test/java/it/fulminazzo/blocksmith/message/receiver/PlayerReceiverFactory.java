package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.message.Player;
import org.jetbrains.annotations.NotNull;

public final class PlayerReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        return new PlayerReceiver((Player) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return Player.class.isAssignableFrom(receiverType);
    }

}
