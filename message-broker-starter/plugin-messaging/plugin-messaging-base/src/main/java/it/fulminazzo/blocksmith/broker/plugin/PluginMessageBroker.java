package it.fulminazzo.blocksmith.broker.plugin;

import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.broker.AbstractMessageBroker;
import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannel;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ServiceLoader;
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
    private static final @Nullable PluginMessageQueryEngineFactory QUERY_ENGINE_FACTORY = ServiceLoader
            .load(PluginMessageQueryEngineFactory.class, PluginMessageQueryEngineFactory.class.getClassLoader())
            .findFirst().orElse(null);

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
        if (QUERY_ENGINE_FACTORY != null) {
            PluginMessageQueryEngine queryEngine = QUERY_ENGINE_FACTORY.create(channelName, registrar);
            return registerChannel(channelBuilder.apply(queryEngine, mapper));
        } else throw new IllegalStateException(
                String.format("Could not find any %s provider. ", PluginMessageQueryEngine.class.getSimpleName()) +
                        String.format("Please check that a platform module of %s is correctly installed.", ProjectInfo.MODULE_NAME)
        );
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
