package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;
import org.jspecify.annotations.NonNull;

/**
 * Special implementation of {@link CommandRegistry} for Bukkit platforms with extended support for Brigadier.
 */
final class BrigadierBukkitCommandRegistry<S> extends BukkitCommandRegistry {
    private final @NotNull BrigadierParser<S> parser = new BrigadierParser<>(this);
    private final @NotNull CommandDispatcher<S> commandDispatcher;

    /**
     * Instantiates a new Bukkit command registry.
     *
     * @param application       the application that is initializing the registry
     * @param commandDispatcher the command dispatcher to register the commands with
     */
    @SuppressWarnings("unchecked")
    public BrigadierBukkitCommandRegistry(final @NotNull ApplicationHandle application,
                                          final @NotNull Object commandDispatcher) {
        super(application);
        this.commandDispatcher = (CommandDispatcher<S>) commandDispatcher;
    }

    @Override
    protected @NotNull CommandSenderWrapper wrapSender(@NotNull Object executor) {
        if (!(executor instanceof CommandSender))
            executor = Reflect.on(executor).call("getBukkitSender").get();
        return super.wrapSender(executor);
    }

    @Override
    protected void actualRegister(final @NonNull String commandName, final @NonNull LiteralNode command) {
        LiteralCommandNode<S> commandNode = parser.parse(command);
        commandDispatcher.getRoot().addChild(commandNode);
    }

}
