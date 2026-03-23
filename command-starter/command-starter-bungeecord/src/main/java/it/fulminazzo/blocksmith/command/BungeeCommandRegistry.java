package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.BlocksmithApplication;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class BungeeCommandRegistry extends CommandRegistry {
    private final @NotNull Map<String, Command> registeredCommands = new ConcurrentHashMap<>();

    private final @NotNull Plugin plugin;
    private final @NotNull PluginManager pluginManager;

    public BungeeCommandRegistry(final @NotNull BlocksmithApplication application) {
        super(application.getMessenger(), application.getLog(), application.getName().toLowerCase());
        this.plugin = (Plugin) application;
        this.pluginManager = ((ProxyServer) application.getServer()).getPluginManager();
    }

    @Override
    protected @NotNull CommandSenderWrapper wrapSender(final @NotNull Object executor) {
        return new BungeeCommandSenderWrapper((CommandSender) executor);
    }

    @Override
    protected void onRegister(final @NotNull String commandName, final @NotNull LiteralNode command) {
        BungeeCommand cmd = new BungeeCommand(commandName, command);
        registeredCommands.put(commandName, cmd);
        pluginManager.registerCommand(plugin, cmd);
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        pluginManager.unregisterCommand(registeredCommands.remove(commandName));
    }

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

    private final class BungeeCommand extends Command implements TabExecutor {
        private final @NotNull LiteralNode command;

        public BungeeCommand(final @NotNull String commandName, final @NotNull LiteralNode command) {
            super(commandName,
                    command.getCommandInfo().orElseThrow().getPermission().getPermission(),
                    command.getAliases().stream()
                            .filter(a -> !a.equals(commandName))
                            .toArray(String[]::new)
            );
            this.command = command;
        }

        @Override
        public void execute(final CommandSender sender, final String[] args) {
            BungeeCommandRegistry.this.execute(command, sender, getName(), args);
        }

        @Override
        public @NotNull Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
            return BungeeCommandRegistry.this.tabComplete(command, sender, getName(), args);
        }

    }

}
