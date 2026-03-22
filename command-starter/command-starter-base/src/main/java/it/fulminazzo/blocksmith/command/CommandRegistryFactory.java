package it.fulminazzo.blocksmith.command;

import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

/**
 * Factory for {@link CommandRegistry}.
 */
public interface CommandRegistryFactory {

    /**
     * Creates a new Command registry.
     *
     * @param prefix the prefix to prepend to automatically computed permissions
     * @return the command registry
     */
    @NotNull CommandRegistry newRegistry(final @NotNull String prefix);

    /**
     * Looks up the available services for a valid factory.
     * Then, it creates a new registry with the given prefix.
     *
     * @param prefix the prefix to prepend to automatically computed permissions
     * @return the command registry
     */
    static @NotNull CommandRegistry newCommandRegistry(final @NotNull String prefix) {
        ServiceLoader<CommandRegistryFactory> loader = ServiceLoader.load(CommandRegistryFactory.class);
        CommandRegistryFactory factory = loader.findFirst().orElseThrow(() -> new IllegalStateException("No valid CommandRegistryFactory was found"));
        return factory.newRegistry(prefix);
    }

}
