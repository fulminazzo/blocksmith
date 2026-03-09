package it.fulminazzo.blocksmith.message.receiver;

import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link ReceiverFactory} for Velocity platform.
 */
public final class VelocityReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        return new VelocityReceiver((CommandSource) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return CommandSource.class.isAssignableFrom(receiverType);
    }

}
