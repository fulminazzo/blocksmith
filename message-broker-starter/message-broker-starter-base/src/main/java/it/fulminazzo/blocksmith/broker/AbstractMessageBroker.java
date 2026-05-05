package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of the {@link MessageBroker} interface.
 * Provides support for registering and automatically closing channels from this broker.
 *
 * @param <S> the type of the channel settings
 */
public abstract class AbstractMessageBroker<S extends MessageChannelSettings<S>> implements MessageBroker<S> {
    private final @NotNull List<MessageChannel> registeredChannels = new ArrayList<>();

    /**
     * Registers a new channel (so that it can be closed by calling {@link #close()} on this object).
     *
     * @param <T>     the type of the channel
     * @param channel the channel to register
     * @return the registered channel
     */
    protected <T extends MessageChannel> @NotNull T registerChannel(final @NotNull T channel) {
        getRegisteredChannels().add(channel);
        return channel;
    }

    private List<MessageChannel> getRegisteredChannels() {
        registeredChannels.removeIf(MessageChannel::isClosed);
        return registeredChannels;
    }

    @Override
    public void close() throws IOException {
        for (MessageChannel channel : getRegisteredChannels()) channel.close();
        registeredChannels.clear();
    }

}
