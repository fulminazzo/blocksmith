package it.fulminazzo.blocksmith.broker.memory;

import it.fulminazzo.blocksmith.broker.AbstractMessageBroker;
import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannel;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BiFunction;

/**
 * Implementation of {@link MessageBroker} for memory-based message channels.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         Mapper mapper = ...;
 *         MemoryMessageBroker messageBroker = MemoryMessageBroker.create(mapper);
 *         }</pre>
 *     </li>
 *     <li>creating a standard channel:
 *         <pre>{@code
 *         MemoryMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 new MemoryMessageChannelSettings()
 *                          .withChannelName("memory_channel")
 *                          .direct("private_channel")
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom channel:
 *         <pre>{@code
 *         MemoryMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 (engine, mapper) -> new CustomMemoryMessageChannel(engine, mapper),
 *                 new MemoryMessageChannelSettings()
 *                         .withChannelName("memory_channel")
 *                         .direct("private_channel")
 *         );
 *         }</pre>
 *         where CustomMemoryMessageChannel extends MemoryMessageChannel and adds custom behavior.
 *     </li>
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class MemoryMessageBroker extends AbstractMessageBroker<MemoryMessageChannelSettings> {
    private final @NotNull Mapper mapper;

    @Override
    public @NotNull MessageChannel newChannel(final @NotNull MemoryMessageChannelSettings settings) {
        return newChannel(MemoryMessageChannel::new, settings);
    }

    /**
     * Creates a new custom channel.
     *
     * @param <C>            the type of the channel
     * @param channelBuilder the channel creation function
     * @param settings       the settings to build the channel with
     * @return the channel
     */
    public <C extends MemoryMessageChannel> @NotNull C newChannel(
            final @NotNull BiFunction<MemoryMessageQueryEngine, Mapper, C> channelBuilder,
            final @NotNull MemoryMessageChannelSettings settings
    ) {
        String channelName = settings.getChannelName();
        if (settings.getChannelType() == MessageChannelType.DIRECT)
            channelName += ":" + settings.getSubchannelName();
        MemoryMessageQueryEngine queryEngine = new MemoryMessageQueryEngine(channelName);
        return registerChannel(channelBuilder.apply(queryEngine, mapper));
    }

    /**
     * Creates a new Memory message broker.
     *
     * @param mapper the mapper to serialize messages with
     * @return the memory message broker
     */
    public static @NotNull MemoryMessageBroker create(final @NotNull Mapper mapper) {
        return new MemoryMessageBroker(mapper);
    }

}
