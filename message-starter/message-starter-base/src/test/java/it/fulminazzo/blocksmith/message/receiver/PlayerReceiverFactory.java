package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.ServerApplication;
import it.fulminazzo.blocksmith.message.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class PlayerReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull ReceiverFactory setup(final @NotNull ServerApplication application) {
        return this;
    }

    @Override
    public @NotNull Collection<Receiver> getAllReceivers() {
        return Player.ALL_PLAYERS.stream().map(this::create).collect(Collectors.toList());
    }

    @Override
    public <R> @NotNull Receiver create(final @NotNull R receiver) {
        return new PlayerReceiver((Player) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return Player.class.isAssignableFrom(receiverType);
    }

}
