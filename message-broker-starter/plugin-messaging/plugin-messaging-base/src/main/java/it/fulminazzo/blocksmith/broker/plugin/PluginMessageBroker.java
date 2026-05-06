package it.fulminazzo.blocksmith.broker.plugin;

import it.fulminazzo.blocksmith.broker.AbstractMessageBroker;
import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannel;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Implementation of {@link MessageBroker} for plugin messaging channels.
 * Requires a backend registrar to manage the internal channels.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         PluginMessageRegistrar registrar = ...;
 *         Mapper mapper = ...;
 *         PluginMessageBroker messageBroker = PluginMessageBroker.create(registrar, mapper);
 *         }</pre>
 *     </li>
 *     <li>creating a standard channel:
 *         <pre>{@code
 *         PluginMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 new PluginMessageChannelSettings()
 *                          .withChannelName("plugin_channel")
 *                          .direct("private_channel")
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom channel:
 *         <pre>{@code
 *         PluginMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 (engine, mapper) -> new CustomPluginMessageChannel(engine, mapper),
 *                 new PluginMessageChannelSettings()
 *                          .withChannelName("plugin_channel")
 *                          .direct("private_channel")
 *         );
 *         }</pre>
 *         where CustomPluginMessageChannel extends PluginMessageChannel and adds custom behavior
 *         and must be compatible with the {@link PluginMessageRegistrar} backend.
 *     </li>
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class PluginMessageBroker extends AbstractMessageBroker<PluginMessageChannelSettings> {
    private final @NotNull PluginMessageRegistrar registrar;
    private final @NotNull Mapper mapper;

    @Override
    public @NotNull MessageChannel newChannel(final @NotNull PluginMessageChannelSettings settings) {
        return newChannel(PluginMessageChannel::new, settings);
    }

    /**
     * Creates a new custom channel.
     *
     * @param <C>            the type of the channel
     * @param channelBuilder the channel creation function
     * @param settings       the settings to build the channel with
     * @return the channel
     */
    public <C extends PluginMessageChannel> @NotNull C newChannel(
            final @NotNull BiFunction<PluginMessageQueryEngine, Mapper, C> channelBuilder,
            final @NotNull PluginMessageChannelSettings settings
    ) {
        String channelName = settings.getChannelName();
        if (settings.getChannelType() == MessageChannelType.DIRECT)
            channelName += ":" + settings.getSubchannelName();
        PluginMessageQueryEngine queryEngine = new PluginMessageQueryEngine(channelName, registrar); //TODO: no!! Factory required
        return registerChannel(channelBuilder.apply(queryEngine, mapper));
    }

    /**
     * Creates a new Plugin message broker.
     *
     * @param registrar the registrar for the internal registration of channels
     * @param mapper    the mapper to serialize messages with
     * @return the plugin message broker
     */
    public static @NotNull PluginMessageBroker create(final @NotNull PluginMessageRegistrar registrar,
                                                      final @NotNull Mapper mapper) {
        return new PluginMessageBroker(registrar, mapper);
    }

}
