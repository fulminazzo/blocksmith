package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Special implementation of {@link CommandRegistry} for Bukkit platforms with extended support for Brigadier.
 *
 * @param <S> the type of the command sender (for Brigadier)
 */
@SuppressWarnings("unchecked")
final class BrigadierBukkitCommandRegistry<S> extends BukkitCommandRegistry {
    private final @NotNull BrigadierParser<S> parser = new BrigadierParser<>(this);

    private final @NotNull RootCommandNode<S> cachedRoot;

    private final @NotNull Map<String, LiteralNode> registeredCommands = new ConcurrentHashMap<>();
    private final @NotNull Map<String, CommandNode<S>> previousBrigadierNodes = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Brigadier bukkit command registry.
     *
     * @param application       the application
     * @param commandDispatcher the initial command dispatcher
     */
    public BrigadierBukkitCommandRegistry(final @NotNull ApplicationHandle application,
                                          final @NotNull Object commandDispatcher) {
        super(application);
        CommandDispatcher<S> brigadierDispatcher = (CommandDispatcher<S>) commandDispatcher;
        this.cachedRoot = brigadierDispatcher.getRoot();
    }

    @Override
    protected @NotNull CommandSenderWrapper wrapSender(@NotNull Object executor) {
        if (!(executor instanceof CommandSender))
            executor = Reflect.on(executor).call("getBukkitSender").get();
        return super.wrapSender(executor);
    }

    @Override
    protected void onRegister(final @NotNull String commandName, final @NotNull LiteralNode command) {
        if (NMSUtils.getServerVersion() < 19) super.registerInCommandMap(commandName, command);

        LiteralCommandNode<S> brigadierNode = parser.parse(command);
        injectIntoBrigadier(commandName, brigadierNode);
        registeredCommands.put(commandName, command);

        for (String alias : command.getAliases()) {
            if (alias.equals(commandName)) continue;
            injectIntoBrigadier(alias, LiteralArgumentBuilder.<S>literal(alias)
                    .redirect(brigadierNode)
                    .build());
        }

        updateCommands();
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        removeChild(commandName);
        LiteralNode command = registeredCommands.remove(commandName);
        if (command != null)
            command.getAliases().stream()
                    .filter(a -> !a.equals(commandName))
                    .forEach(this::restoreIntoBrigadier);

        if (NMSUtils.getServerVersion() < 19) super.onUnregister(commandName);

        updateCommands();
    }

    private void injectIntoBrigadier(final @NotNull String name,
                                     final @NotNull LiteralCommandNode<S> node) {
        RootCommandNode<S> root = getRoot();
        CommandNode<S> previous = root.getChild(name);
        if (previous != null) {
            previousBrigadierNodes.put(name, previous);
            removeChild(name);
        }
        root.addChild(node);
        if (!name.startsWith(getBukkitPrefix())) {
            String alias = getBukkitPrefix() + name;
            injectIntoBrigadier(alias, LiteralArgumentBuilder.<S>literal(alias)
                    .redirect(node)
                    .build());
        }
    }

    private void restoreIntoBrigadier(final @NotNull String name) {
        RootCommandNode<S> root = getRoot();
        removeChild(name);
        CommandNode<S> previous = previousBrigadierNodes.remove(name);
        if (previous != null) root.addChild(previous);
        if (!name.startsWith(getBukkitPrefix()))
            restoreIntoBrigadier(getBukkitPrefix() + name);
    }

    private void removeChild(final @NonNull String name) {
        Reflect root = Reflect.on(getRoot());
        root.field("children").call("remove", name);
        root.field("literals").call("remove", name);
    }

    private void updateCommands() {
        for (Player player : server.getOnlinePlayers())
            player.updateCommands();
    }

    private @NotNull RootCommandNode<S> getRoot() {
        Optional<?> liveDispatcher = NMSUtils.getCommandDispatcher(server);
        if (liveDispatcher.isPresent()) {
            try {
                return ((CommandDispatcher<S>) liveDispatcher.get()).getRoot();
            } catch (ClassCastException ignored) {
            }
        }
        return cachedRoot;
    }

}
