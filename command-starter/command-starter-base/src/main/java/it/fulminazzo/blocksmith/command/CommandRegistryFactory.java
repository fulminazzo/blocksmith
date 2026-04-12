//TODO: update
//package it.fulminazzo.blocksmith.command;
//
//import it.fulminazzo.blocksmith.ApplicationHandle;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ServiceLoader;
//
///**
// * Factory for {@link CommandRegistry}.
// */
//public interface CommandRegistryFactory {
//
//    /**
//     * Creates a new Command registry.
//     *
//     * @param application the application that is initializing the registry
//     * @return the command registry
//     */
//    @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application);
//
//    /**
//     * Looks up the available services for a valid factory.
//     * Then, it creates a new registry with the given prefix.
//     *
//     * @param application the application that is initializing the registry
//     * @return the command registry
//     */
//    static @NotNull CommandRegistry newCommandRegistry(final @NotNull ApplicationHandle application) {
//        ServiceLoader<CommandRegistryFactory> loader = ServiceLoader.load(CommandRegistryFactory.class, CommandRegistryFactory.class.getClassLoader());
//        CommandRegistryFactory factory = loader.findFirst().orElseThrow(() -> new IllegalStateException("No valid CommandRegistryFactory was found"));
//        return factory.newRegistry(application);
//    }
//
//}
