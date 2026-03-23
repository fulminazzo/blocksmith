package it.fulminazzo.blocksmith.command;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import it.fulminazzo.blocksmith.BlocksmithApplication;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

final class VelocityCommandRegistry extends CommandRegistry {
    private final @NotNull CommandManager commandManager;
    private final @NotNull Map<String, Set<String>> aliases = new ConcurrentHashMap<>();

    public VelocityCommandRegistry(final @NotNull BlocksmithApplication application) {
        super(application.getMessenger(), application.getLog(), application.getName().toLowerCase());
        this.commandManager = ((ProxyServer) application.getServer()).getCommandManager();
    }

    @Override
    protected @NotNull CommandSenderWrapper wrapSender(final @NotNull Object executor) {
        return new VelocityCommandSenderWrapper((CommandSource) executor);
    }

    @Override
    protected void onRegister(final @NotNull String commandName, final @NotNull LiteralNode command) {
        aliases.put(commandName, command.getAliases());
        List<String> aliases = new ArrayList<>(command.getAliases());
        aliases.remove(commandName);
        CommandMeta meta = commandManager.metaBuilder(commandName)
                .aliases(aliases.toArray(new String[0]))
                .build();
        commandManager.register(meta, new VelocityCommand(command));
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        commandManager.unregister(commandName);
        Set<String> aliases = this.aliases.remove(commandName);
        if (aliases != null) aliases.forEach(commandManager::unregister);
    }

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSource.class;
    }

    @RequiredArgsConstructor
    final class VelocityCommand implements SimpleCommand {
        private final @NotNull LiteralNode command;

        @Override
        public void execute(final @NotNull Invocation invocation) {
            VelocityCommandRegistry.this.execute(command, invocation.source(), invocation.alias(), invocation.arguments());
        }

        @Override
        public @NotNull CompletableFuture<List<String>> suggestAsync(final @NotNull Invocation invocation) {
            return CompletableFuture.supplyAsync(() ->
                    VelocityCommandRegistry.this.tabComplete(command, invocation.source(), invocation.alias(), invocation.arguments())
            );
        }

        @Override
        public boolean hasPermission(final @NotNull Invocation invocation) {
            return wrapSender(invocation.source()).hasPermission(command.getCommandInfo().orElseThrow().getPermission());
        }

    }

}
