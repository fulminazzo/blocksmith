package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.scheduler.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class OfflinePlayerArgumentParser implements ArgumentParser<OfflinePlayer> {
    private final @NotNull Set<String> names = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("deprecation")
    @Override
    public @NonNull OfflinePlayer parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        Server server = (Server) context.getApplication().getServer();
        String argument = context.getCurrent();
        if (getNames(context.getApplication()).stream().anyMatch(argument::equalsIgnoreCase))
            return server.getOfflinePlayer(argument);
        else throw new CommandExecutionException("error.player-not-found")
                .arguments(Placeholder.of("player", argument));
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
        return new ArrayList<>(getNames(context.getApplication()));
    }

    private @NotNull Set<String> getNames(final @NotNull ApplicationHandle application) {
        Server server = (Server) application.getServer();
        if (names.isEmpty()) {
            names.add("<player>");
            Scheduler.schedule(application, t -> {
                Arrays.stream(server.getOfflinePlayers())
                        .map(OfflinePlayer::getName)
                        .forEach(names::add);
                names.remove("<player>");
            }).async().run();
        }
        server.getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(names::add);
        return names;
    }

}
