package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.*;
import it.fulminazzo.blocksmith.command.argument.dto.Coordinate;
import it.fulminazzo.blocksmith.command.argument.dto.Position;
import it.fulminazzo.blocksmith.command.argument.dto.WorldPosition;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.command.visitor.usage.UsageStyle;
import it.fulminazzo.blocksmith.conversion.Convertible;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class BukkitCommandRegistryFactory implements CommandRegistryFactory {

    static {
        ArgumentParsers.register(Player.class, new ArgumentParser<>() {

            @Override
            public @NotNull Player parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                Server server = visitor.getApplication().server();
                CommandSender sender = (CommandSender) visitor.getCommandSender().handle();
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
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                Server server = visitor.getApplication().server();
                CommandSender sender = (CommandSender) visitor.getCommandSender().handle();
                final Collection<Player> players = new ArrayList<>(server.getOnlinePlayers());
                if (sender instanceof Player) {
                    Player senderPlayer = (Player) sender;
                    players.removeIf(p -> !senderPlayer.canSee(p));
                }
                return players.stream().map(Player::getName).collect(Collectors.toList());
            }

        });
        UsageStyle.registerDefaultArgumentColor(Player.class, UsageStyle.DEFAULT_PLAYER_COLOR);
        ArgumentParsers.register(ConsoleCommandSender.class, new ArgumentParser<>() {

            @Override
            public @NotNull ConsoleCommandSender parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                String current = visitor.getInput().getCurrent();
                if (current.equals(CommandSenderWrapper.CONSOLE_COMMAND_NAME)) {
                    Server server = visitor.getApplication().server();
                    return server.getConsoleSender();
                } else throw new ArgumentParseException(CommandMessages.UNRECOGNIZED_ARGOMENT)
                        .arguments(
                                Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, current),
                                Placeholder.of("expected", CommandSenderWrapper.CONSOLE_COMMAND_NAME)
                        );
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                return List.of(CommandSenderWrapper.CONSOLE_COMMAND_NAME);
            }

        });
        ArgumentParsers.register(CommandSender.class, new CompositeArgumentParser<>(ConsoleCommandSender.class, Player.class));
        UsageStyle.registerDefaultArgumentColor(CommandSender.class, UsageStyle.DEFAULT_PLAYER_COLOR);
        ArgumentParsers.register(CommandSenderWrapper.class, new CommandSenderWrapperArgumentParser<>(CommandSender.class));
        UsageStyle.registerDefaultArgumentColor(CommandSenderWrapper.class, UsageStyle.DEFAULT_PLAYER_COLOR);
        ArgumentParsers.register(OfflinePlayer.class, new OfflinePlayerArgumentParser());
        UsageStyle.registerDefaultArgumentColor(Player.class, UsageStyle.DEFAULT_PLAYER_COLOR);
        ArgumentParsers.register(World.class, new ArgumentParser<>() {

            @Override
            public @NotNull World parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                Server server = visitor.getApplication().server();
                String argument = visitor.getInput().getCurrent();
                World world = server.getWorld(argument);
                if (world != null) return world;
                else throw new ArgumentParseException(CommandMessages.WORLD_NOT_FOUND)
                        .arguments(Placeholder.of("world", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                Server server = visitor.getApplication().server();
                return server.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            }

        });
        UsageStyle.registerDefaultArgumentColor(World.class, UsageStyle.DEFAULT_POSITION_COLOR);
        ArgumentParsers.register(WorldPosition.class, new MultiArgumentParser<>(
                l -> {
                    World world = (World) l.get(0);
                    Position position = (Position) l.get(1);
                    return new WorldPosition(world.getName(), position.getX(), position.getY(), position.getZ());
                },
                World.class, Position.class
        ));
        UsageStyle.registerDefaultArgumentColor(WorldPosition.class, UsageStyle.DEFAULT_POSITION_COLOR);
        ArgumentParsers.register(Location.class, new DelegateArgumentParser<>(
                (v, p) -> p.as(Location.class, v.getCommandSender()),
                WorldPosition.class
        ));
        UsageStyle.registerDefaultArgumentColor(Location.class, UsageStyle.DEFAULT_POSITION_COLOR);

        Convertible.register(Position.class, Location.class, (p, a) -> {
            Location start = new Location(null, 0, 0, 0);
            List<World> worlds = Bukkit.getWorlds();
            if (!worlds.isEmpty()) start = worlds.get(0).getSpawnLocation();
            start = getStartLocation(start, a);
            return buildLocation(start.getWorld(), p.getX(), p.getY(), p.getZ(), start);
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

    private static @NotNull Location getStartLocation(final @NotNull Location start,
                                                      final @Nullable Object @NotNull ... args) {
        if (args.length > 0) {
            Object arg = args[0];
            if (arg instanceof CommandSenderWrapper<?>) {
                CommandSenderWrapper<?> sender = (CommandSenderWrapper<?>) arg;
                if (sender.isPlayer()) {
                    Player player = (Player) sender.handle();
                    return player.getLocation();
                }
            } else if (arg instanceof Location) return (Location) arg;
            else if (arg instanceof World) return ((World) arg).getSpawnLocation();
            else if (arg instanceof String) return Bukkit.getWorld((String) arg).getSpawnLocation();
        }
        return start;
    }

}
