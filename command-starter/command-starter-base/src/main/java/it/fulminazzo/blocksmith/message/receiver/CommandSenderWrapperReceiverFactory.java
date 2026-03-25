package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of {@link ReceiverFactory} for {@link CommandSenderWrapper} objects.
 */
public final class CommandSenderWrapperReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull Collection<Receiver> getAllReceivers() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        CommandSenderWrapper wrapper = (CommandSenderWrapper) receiver;
        Object internal = wrapper.getActualSender();
        return ReceiverFactories.get(internal.getClass()).create(internal);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return CommandSenderWrapper.class.isAssignableFrom(receiverType);
    }

}
