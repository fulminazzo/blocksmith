//TODO: update
//package it.fulminazzo.blocksmith.command;
//
//import com.mojang.brigadier.tree.LiteralCommandNode;
//import it.fulminazzo.blocksmith.ApplicationHandle;
//import it.fulminazzo.blocksmith.command.node.LiteralNode;
//import org.jetbrains.annotations.NotNull;
//
///**
// * A special type of {@link CommandRegistry} that uses the Mojang Brigadier system
// * to parse and register commands.
// *
// * @param <S> the type of the command sender (for Brigadier)
// */
//abstract class BrigadierCommandRegistry<S> extends CommandRegistry {
//    private final @NotNull BrigadierParser<S> parser = new BrigadierParser<>(this);
//
//    /**
//     * Instantiates a new Brigadier command registry.
//     *
//     * @param application the application
//     */
//    public BrigadierCommandRegistry(final @NotNull ApplicationHandle application) {
//        super(application);
//    }
//
//    @Override
//    protected void onRegister(final @NotNull String commandName, final @NotNull LiteralNode command) {
//        command.getAliases().forEach(a -> onRegisterSingle(a, command.clone(a)));
//    }
//
//    /**
//     * Method called upon actively registering a command.
//     * <br>
//     * <b>WARNING</b>: will NOT register the aliases!
//     *
//     * @param commandName the command name
//     * @param command     the root of the command route
//     */
//    protected void onRegisterSingle(final @NotNull String commandName, final @NotNull LiteralNode command) {
//        onRegister(commandName, command, parser.parse(command));
//    }
//
//    /**
//     * Method called upon actively registering a command.
//     *
//     * @param commandName      the command name
//     * @param command          the root of the command route
//     * @param brigadierCommand the brigadier command obtained from the blocksmith command
//     */
//    protected abstract void onRegister(final @NotNull String commandName,
//                                       final @NotNull LiteralNode command,
//                                       final @NotNull LiteralCommandNode<S> brigadierCommand);
//
//}
