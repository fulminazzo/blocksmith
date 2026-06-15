package it.fulminazzo.blocksmith.broker.plugin;

import it.fulminazzo.blocksmith.broker.AbstractMessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannel;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinator;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * Message broker for the Minecraft plugin messaging system.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         PluginMessageBroker messageBroker = PluginMessageBroker.builder()
 *                 // the owner of the broker, generally the plugin
 *                 .owner(plugin)
 *                 .build()
 *         }</pre>
 *     </li>
 *     <li>creation with injected {@link PluginMessageChannelCoordinator} ({@link #close()} will close it):
 *         <pre>{@code
 *         PluginMessageBroker messageBroker = PluginMessageBroker.builder()
 *                 .coordinator(coordinator)
 *                 .build()
 *         }</pre>
 *     </li>
 *     <li>creating a standard channel:
 *         <pre>{@code
 *         PluginMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 new PluginMessageChannelSettings()
 *                         .withChannelName("minecraft_channel")
 *                         .direct("private_channel")
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom channel:
 *         <pre>{@code
 *         PluginMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 (engine, mapper) -> new CustomPluginMessageChannel(engine, mapper),
 *                 new PluginMessageChannelSettings()
 *                         .withChannelName("minecraft_channel")
 *                         .direct("private_channel")
 *         );
 *         }</pre>
 *         where CustomPluginMessageChannel extends {@link PluginMessageChannel} and adds custom behavior.
 *     </li>
 * </ul>
 *
 * @see PluginMessageChannel
 * @see PluginMessageChannelSettings
 * @see PluginMessageQueryEngine
 */
public final class PluginMessageBroker extends AbstractMessageBroker<PluginMessageChannelSettings> {
    private final @NotNull ExecutorService executor;

    private final @NotNull PluginMessageChannelCoordinator coordinator;

    /**
     * Instantiates a new Plugin message broker.
     *
     * @param executor    the executor
     * @param coordinator the coordinator
     * @param mapper      the mapper
     */
    PluginMessageBroker(
            final @NotNull ExecutorService executor,
            final @NotNull PluginMessageChannelCoordinator coordinator,
            final @NotNull Mapper mapper
    ) {
        super(mapper);
        this.executor = executor;
        this.coordinator = coordinator;
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
        PluginMessageQueryEngine queryEngine = new PluginMessageQueryEngine(
                executor,
                channelName,
                coordinator
        );
        return registerChannel(channelBuilder.apply(queryEngine, mapper));
    }

    @Override
    public @NotNull MessageChannel newChannel(final @NotNull PluginMessageChannelSettings settings) {
        return newChannel(PluginMessageChannel::new, settings);
    }

    @Override
    public void close() {
        super.close();
        coordinator.close();
        executor.shutdown();
    }

    /**
     * Gets a new builder for this class.
     *
     * @return the builder
     */
    public static @NotNull PluginMessageBrokerBuilder builder() {
        return new PluginMessageBrokerBuilder();
    }
    
}
