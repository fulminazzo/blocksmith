package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.scheduler.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class OfflinePlayerArgumentParser implements ArgumentParser<OfflinePlayer> {
    private final @NotNull Set<String> names = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull OfflinePlayer parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
        Server server = visitor.getApplication().server();
        String argument = visitor.getInput().getCurrent();
        if (getNames(visitor.getApplication()).stream().anyMatch(argument::equalsIgnoreCase))
            return server.getOfflinePlayer(argument);
        else throw new ArgumentParseException(CommandMessages.PLAYER_NOT_FOUND)
                .arguments(Placeholder.of("player", argument));
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        return new ArrayList<>(getNames(visitor.getApplication()));
    }

    private @NotNull Set<String> getNames(final @NotNull ApplicationHandle application) {
        Server server = application.server();
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
