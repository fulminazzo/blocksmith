package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.argument.MultiArgumentParser;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class BukkitCommandRegistryFactory implements CommandRegistryFactory {

    static {
        ArgumentParsers.register(Player.class, new ArgumentParser<>() {

            @Override
            public @NonNull Player parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
                Server server = visitor.getApplication().server();
                CommandSender sender = (CommandSender) visitor.getCommandSender().getActualSender();
                String argument = visitor.getInput().getCurrent();
                Player player = server.getPlayer(argument);
                if (player != null) {
                    if (sender instanceof Player) {
                        Player senderPlayer = (Player) sender;
                        if (senderPlayer.canSee(player)) return player;
                    } else return player;
                }
                throw new ArgumentParseException("error.player-not-found")
                        .arguments(Placeholder.of("player", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
                Server server = visitor.getApplication().server();
                CommandSender sender = (CommandSender) visitor.getCommandSender().getActualSender();
                final Collection<Player> players = new ArrayList<>(server.getOnlinePlayers());
                if (sender instanceof Player) {
                    Player senderPlayer = (Player) sender;
                    players.removeIf(p -> !senderPlayer.canSee(p));
                }
                return players.stream().map(Player::getName).collect(Collectors.toList());
            }

        });
        ArgumentParsers.register(OfflinePlayer.class, new OfflinePlayerArgumentParser());
        ArgumentParsers.register(World.class, new ArgumentParser<>() {

            @Override
            public @NotNull World parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
                Server server = visitor.getApplication().server();
                String argument = visitor.getInput().getCurrent();
                World world = server.getWorld(argument);
                if (world != null) return world;
                else throw new ArgumentParseException("error.world-not-found")
                        .arguments(Placeholder.of("world", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
                Server server = visitor.getApplication().server();
                return server.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            }

        });
        ArgumentParsers.register(Location.class, new MultiArgumentParser<>(
                l -> new Location(null, (double) l.get(0), (double) l.get(1), (double) l.get(2)),
                Double.class, Double.class, Double.class
        ));
    }

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application) {
        return NMSUtils.getCommandDispatcher(application.server())
                .map(d -> new BrigadierBukkitCommandRegistry<>(application, d))
                .map(r -> (CommandRegistry) r)
                .orElse(new BukkitCommandRegistry(application));
    }

}
