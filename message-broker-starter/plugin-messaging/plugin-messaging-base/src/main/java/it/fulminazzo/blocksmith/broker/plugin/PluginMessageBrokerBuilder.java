package it.fulminazzo.blocksmith.broker.plugin;

import it.fulminazzo.blocksmith.broker.AbstractMessageBrokerBuilder;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinator;
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactories;
import it.fulminazzo.blocksmith.util.ThreadUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A builder for {@link PluginMessageBroker}.
 * <br>
 * Example usage:
 * <pre>{@code
 * PluginMessageBroker messageBroker = PluginMessageBroker.builder()
 *         .owner(plugin)
 *         .mapper(MapperFormat.SERIALIZABLE.newMapper()) // defaults to JSON
 *         .build();
 * }</pre>
 *
 * @see PluginMessageBroker
 */
public final class PluginMessageBrokerBuilder
        extends AbstractMessageBrokerBuilder<PluginMessageBroker, PluginMessageBrokerBuilder> {
    private @Nullable ExecutorService executor;

    private @Nullable PluginMessageChannelCoordinator coordinator;

    /**
     * Generates and sets a {@link PluginMessageChannelCoordinator} for the broker from the given owner.
     * <br>
     * <b>WARNING</b>: owner must be of a supported type.
     *
     * @param owner the owner of the broker
     * @return this object (for method chaining)
     */
    public @NotNull PluginMessageBrokerBuilder owner(final @NotNull Object owner) {
        return coordinator(PluginMessageChannelCoordinatorFactories.create(owner));
    }

    /**
     * Sets the channel coordinator of the broker.
     *
     * @param coordinator the coordinator
     * @return this object (for method chaining)
     * @see PluginMessageChannelCoordinator
     */
    public @NotNull PluginMessageBrokerBuilder coordinator(final @NotNull PluginMessageChannelCoordinator coordinator) {
        this.coordinator = coordinator;
        return this;
    }

    /**
     * Sets the executor of the queries.
     *
     * @param executor the executor
     * @return this object (for method chaining)
     */
    public @NotNull PluginMessageBrokerBuilder executor(final @NotNull ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public @NotNull PluginMessageBroker build() {
        final PluginMessageChannelCoordinator actualCoordinator = Objects.requireNonNull(
                coordinator,
                "owner coordinator has not been specified yet"
        );

        final ExecutorService actualExecutor;
        if (executor != null) actualExecutor = executor;
        else actualExecutor = Executors.newCachedThreadPool(
                ThreadUtils.ownedThreadFactory(PluginMessageQueryEngine.class)
        );

        return new PluginMessageBroker(actualExecutor, actualCoordinator, getMapper());
    }

}
