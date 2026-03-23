package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.message.Messenger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ServiceLoader;

/**
 * Factory for {@link CommandRegistry}.
 */
public interface CommandRegistryFactory {

    /**
     * Creates a new Command registry.
     *
     * @param messenger the messenger (to handle internal messages)
     * @param logger    the logger
     * @return the command registry
     */
    @NotNull CommandRegistry newRegistry(final @NotNull Messenger messenger,
                                         final @NotNull Logger logger);

    /**
     * Looks up the available services for a valid factory.
     * Then, it creates a new registry with the given prefix.
     *
     * @param messenger the messenger (to handle internal messages)
     * @param logger    the logger
     * @return the command registry
     */
    static @NotNull CommandRegistry newCommandRegistry(final @NotNull Messenger messenger,
                                                       final @NotNull Logger logger) {
        ServiceLoader<CommandRegistryFactory> loader = ServiceLoader.load(CommandRegistryFactory.class);
        CommandRegistryFactory factory = loader.findFirst().orElseThrow(() -> new IllegalStateException("No valid CommandRegistryFactory was found"));
        return factory.newRegistry(messenger, logger);
    }

}
