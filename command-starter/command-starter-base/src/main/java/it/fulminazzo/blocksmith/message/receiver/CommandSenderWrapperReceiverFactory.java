package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.ServerApplication;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of {@link ReceiverFactory} for {@link CommandSenderWrapper} objects.
 */
public final class CommandSenderWrapperReceiverFactory extends AbstractReceiverFactory {
    private ServerApplication application;

    @Override
    public @NotNull ReceiverFactory setup(final @NotNull ServerApplication application) {
        super.setup(application);
        this.application = application;
        return this;
    }

    @Override
    protected @NotNull Collection<Receiver> getAllReceiversImpl() {
        return Collections.emptyList();
    }

    @Override
    protected @NotNull <R> Receiver createImpl(final @NotNull R receiver) {
        CommandSenderWrapper<?> wrapper = (CommandSenderWrapper<?>) receiver;
        Object internal = wrapper.getActualSender();
        return ReceiverFactories.get(internal.getClass(), application).create(internal);
    }

    @Override
    protected boolean supportsImpl(final @NotNull Class<?> receiverType) {
        return CommandSenderWrapper.class.isAssignableFrom(receiverType);
    }

}
