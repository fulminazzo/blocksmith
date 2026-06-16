package it.fulminazzo.blocksmith.broker;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of the {@link MessageBroker} interface.
 * Provides support for registering and automatically closing channels from this broker.
 *
 * @param <S> the type of the channel settings
 * @see MessageBroker
 * @see MessageChannel
 * @see MessageChannelSettings
 */
public abstract class AbstractMessageBroker<S extends MessageChannelSettings> implements MessageBroker<S> {
    /**
     * The mapper for converting messages into payloads.
     */
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    protected final @NotNull Mapper mapper;

    private final @NotNull List<MessageChannel> registeredChannels = new ArrayList<>();

    /**
     * Instantiates a new Abstract message broker.
     *
     * @param mapper the mapper
     */
    protected AbstractMessageBroker(final @NotNull Mapper mapper) {
        this.mapper = mapper;
    }

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
    public void close() {
        for (MessageChannel channel : getRegisteredChannels()) channel.close();
        registeredChannels.clear();
    }

}
