package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class OfflinePlayerArgumentParser implements ArgumentParser<OfflinePlayer> {

    @Override
    public @NonNull OfflinePlayer parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        Server server = (Server) context.getApplication().getServer();
        String argument = context.getCurrent();
        return Arrays.stream(server.getOfflinePlayers())
                .filter(p -> argument.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElseThrow(() -> new CommandExecutionException("error.player-not-found")
                        .arguments(Placeholder.of("player", argument)));
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
        Server server = (Server) context.getApplication().getServer();
        return Arrays.stream(server.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList());
    }

}
