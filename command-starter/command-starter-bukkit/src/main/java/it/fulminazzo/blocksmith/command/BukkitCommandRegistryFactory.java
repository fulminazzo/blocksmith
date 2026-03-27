package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.arguments.ArgumentType;
import it.fulminazzo.blocksmith.ApplicationHandle;
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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        ArgumentParsers.register(OfflinePlayer.class, new OfflinePlayerArgumentParser());
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
        ArgumentParsers.register(Location.class, new ArgumentParser<>() {

            @SuppressWarnings("DataFlowIssue")
            @Override
            public @NotNull Location parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                ArgumentParser<Double> doubleParser = ArgumentParsers.of(Double.class);
                double x = doubleParser.parse(context);
                if (context.isLast()) throw new CommandExecutionException("error.not-enough-arguments");
                double y = doubleParser.parse(context.advanceCursor());
                if (context.isLast()) throw new CommandExecutionException("error.not-enough-arguments");
                double z = doubleParser.parse(context.advanceCursor());
                return new Location(null, x, y, z);
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                return Collections.singletonList("<x> <y> <z>");
            }

            @SuppressWarnings("DataFlowIssue")
            @Override
            public boolean validateCompletions(final @NotNull CommandExecutionContext context) {
                try {
                    ArgumentParser<Double> doubleParser = ArgumentParsers.of(Double.class);
                    double x = doubleParser.parse(context);
                    if (context.isLast()) return false;
                    double y = doubleParser.parse(context.advanceCursor());
                    if (context.isLast()) return false;
                    double z = doubleParser.parse(context.advanceCursor());
                    context.addParsedArgument(new Location(null, x, y, z));
                    return !context.isLast();
                } catch (CommandExecutionException e) {
                    return false;
                }
            }

        });
        ArgumentTypes.register(Location.class, getPositionArgumentType());
    }

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application) {
        return NMSUtils.getCommandDispatcher((Server) application.getServer())
                .map(d -> new BrigadierBukkitCommandRegistry<>(application, d))
                .map(r -> (CommandRegistry) r)
                .orElse(new BukkitCommandRegistry(application));
    }

    private static @NotNull ArgumentType<?> getPositionArgumentType() {
        try {
            Class<?> positionArgumentType = getPositionArgumentTypeClass();
            Constructor<?> constructor = positionArgumentType.getDeclaredConstructor();
            return (ArgumentType<?>) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not create Position %s", ArgumentType.class.getSimpleName()), e);
        }
    }

    private static @NonNull Class<?> getPositionArgumentTypeClass() throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft.commands.arguments.coordinates.BlockPosArgument");
        } catch (ClassNotFoundException e) {
            return Class.forName(String.format("net.minecraft.server.%s.ArgumentPosition", NMSUtils.getNMSVersion()));
        }
    }

}
