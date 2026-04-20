package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.*;
import it.fulminazzo.blocksmith.command.argument.dto.Coordinate;
import it.fulminazzo.blocksmith.command.argument.dto.Position;
import it.fulminazzo.blocksmith.command.argument.dto.WorldPosition;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.conversion.Convertible;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
                throw new ArgumentParseException(CommandMessages.PLAYER_NOT_FOUND)
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
                else throw new ArgumentParseException(CommandMessages.WORLD_NOT_FOUND)
                        .arguments(Placeholder.of("world", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
                Server server = visitor.getApplication().server();
                return server.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            }

        });
        ArgumentParsers.register(WorldPosition.class, new MultiArgumentParser<>(
                l -> {
                    World world = (World) l.get(0);
                    Position position = (Position) l.get(1);
                    return new WorldPosition(world.getName(), position.getX(), position.getY(), position.getZ());
                },
                World.class, Position.class
        ));
        ArgumentParsers.register(Location.class, new DelegateArgumentParser<>(
                (v, p) -> p.as(Location.class, v.getCommandSender()),
                WorldPosition.class
        ));

        Convertible.register(Position.class, Location.class, (p, a) -> {
            Location start = new Location(null, 0, 0, 0);
            List<World> worlds = Bukkit.getWorlds();
            if (!worlds.isEmpty()) start = worlds.get(0).getSpawnLocation();
            start = getStartLocation(start, a);
            return buildLocation(null, p.getX(), p.getY(), p.getZ(), start);
        });
        Convertible.register(WorldPosition.class, Location.class, (p, a) -> {
            World world = Bukkit.getWorld(p.getWorld());
            Location start = world.getSpawnLocation();
            start = getStartLocation(start, a);
            return buildLocation(world, p.getX(), p.getY(), p.getZ(), start);
        });
    }

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application) {
        return NMSUtils.getCommandDispatcher(application.server())
                .map(d -> new BrigadierBukkitCommandRegistry<>(application, d))
                .map(r -> (CommandRegistry) r)
                .orElse(new BukkitCommandRegistry(application));
    }

    private static Location buildLocation(final @Nullable World world,
                                          final @NotNull Coordinate x,
                                          final @NotNull Coordinate y,
                                          final @NotNull Coordinate z,
                                          final @NotNull Location start) {
        return new Location(
                world,
                (x.isRelative() ? start.getX() : 0) + x.getValue(),
                (y.isRelative() ? start.getY() : 0) + y.getValue(),
                (z.isRelative() ? start.getZ() : 0) + z.getValue()
        );
    }

    private static Location getStartLocation(final @NotNull Location start, final @Nullable Object @NotNull ... args) {
        if (args.length > 0 && args[0] instanceof CommandSenderWrapper<?>) {
            CommandSenderWrapper<?> sender = (CommandSenderWrapper<?>) args[0];
            if (sender.isPlayer()) {
                Player player = (Player) sender.getActualSender();
                return player.getLocation();
            }
        }
        return start;
    }

}
