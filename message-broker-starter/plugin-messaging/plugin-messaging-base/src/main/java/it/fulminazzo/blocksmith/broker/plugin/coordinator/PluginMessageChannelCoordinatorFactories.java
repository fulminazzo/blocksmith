package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds all the {@link PluginMessageChannelCoordinatorFactory} implementations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginMessageChannelCoordinatorFactories {
    private static final @NotNull Set<PluginMessageChannelCoordinatorFactory> FACTORIES = ServiceLoader.load(
                    PluginMessageChannelCoordinatorFactory.class,
                    PluginMessageChannelCoordinatorFactory.class.getClassLoader()
            ).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toSet());

    /**
     * Instantiates a new {@link PluginMessageChannelCoordinator} instance.
     *
     * @param owner the owner of the coordinator
     * @return the coordinator
     */
    public static @NotNull PluginMessageChannelCoordinator create(final @NotNull Object owner) {
        Class<?> ownerType = owner.getClass();
        return FACTORIES.stream()
                .filter(f -> f.supportsOwner(ownerType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory found for owner type: " + ownerType))
                .create(owner);
    }

}
