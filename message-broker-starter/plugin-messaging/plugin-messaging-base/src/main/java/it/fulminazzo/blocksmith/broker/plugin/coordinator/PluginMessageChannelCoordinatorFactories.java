package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Holds all the {@link PluginMessageChannelCoordinatorFactory} implementations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginMessageChannelCoordinatorFactories {
    private static final @NotNull Set<PluginMessageChannelCoordinatorFactory> FACTORIES = new LinkedHashSet<>();

    static {
        registerNext(ServiceLoader.load(
                PluginMessageChannelCoordinatorFactory.class,
                PluginMessageChannelCoordinatorFactories.class.getClassLoader()
        ).iterator());
    }

    /**
     * Instantiates a new {@link PluginMessageChannelCoordinator} instance.
     *
     * @param owner the owner of the coordinator
     * @return the coordinator
     */
    public static @NotNull PluginMessageChannelCoordinator create(final @NotNull Object owner) {
        Class<?> ownerType = owner.getClass();
        for (PluginMessageChannelCoordinatorFactory factory : FACTORIES)
            if (factory.supportsOwner(ownerType)) return factory.create(owner);
        throw new IllegalArgumentException("No factory found for owner type: " + ownerType);
    }

    private static void registerNext(final @NotNull Iterator<PluginMessageChannelCoordinatorFactory> factories) {
        try {
            if (factories.hasNext()) FACTORIES.add(factories.next());
            else return;
        } catch (ServiceConfigurationError | NoClassDefFoundError e) {
            // unsupported factory on the current platform
        }
        registerNext(factories);
    }

}
