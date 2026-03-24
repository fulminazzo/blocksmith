package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.BlocksmithApplication;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class BukkitCommandRegistryFactory implements CommandRegistryFactory {

    static {
        ArgumentParsers.register(Player.class, new ArgumentParser<>() {

            @Override
            public @NonNull Player parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                Server server = (Server) context.getApplication().getServer();
                CommandSender sender = (CommandSender) context.getCommandSender().getActualSender();
                String argument = context.getCurrent();
                Player player = server.getPlayer(argument);
                if (player != null) {
                    if (sender instanceof Player) {
                        Player senderPlayer = (Player) sender;
                        if (senderPlayer.canSee(player)) return player;
                    } else return player;
                }
                throw new CommandExecutionException("error.player-not-found")
                        .arguments(Placeholder.of("player", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                Server server = (Server) context.getApplication().getServer();
                CommandSender sender = (CommandSender) context.getCommandSender().getActualSender();
                final Collection<Player> players = new ArrayList<>(server.getOnlinePlayers());
                if (sender instanceof Player) {
                    Player senderPlayer = (Player) sender;
                    players.removeIf(p -> !senderPlayer.canSee(p));
                }
                return players.stream().map(Player::getName).collect(Collectors.toList());
            }

        });
        ArgumentParsers.register(OfflinePlayer.class, new ArgumentParser<>() {

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

        });
        ArgumentParsers.register(World.class, new ArgumentParser<>() {

            @Override
            public @NotNull World parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                Server server = (Server) context.getApplication().getServer();
                String argument = context.getCurrent();
                World world = server.getWorld(argument);
                if (world != null) return world;
                else throw new CommandExecutionException("error.world-not-found")
                        .arguments(Placeholder.of("world", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                Server server = (Server) context.getApplication().getServer();
                return server.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            }

        });
    }

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull BlocksmithApplication application) {
        return new BukkitCommandRegistry(application);
    }

}
