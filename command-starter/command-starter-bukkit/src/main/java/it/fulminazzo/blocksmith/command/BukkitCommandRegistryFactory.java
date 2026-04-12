//TODO: update
//package it.fulminazzo.blocksmith.command;
//
//import it.fulminazzo.blocksmith.ApplicationHandle;
//import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
//import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
//import it.fulminazzo.blocksmith.message.argument.Placeholder;
//import lombok.RequiredArgsConstructor;
//import org.bukkit.Location;
//import org.bukkit.OfflinePlayer;
//import org.bukkit.Server;
//import org.bukkit.World;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.jetbrains.annotations.NotNull;
//import org.jspecify.annotations.NonNull;
//
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@SuppressWarnings("DataFlowIssue")
//public final class BukkitCommandRegistryFactory implements CommandRegistryFactory {
//
//    static {
//        ArgumentParsers.register(Player.class, new ArgumentParser<>() {
//
//            @Override
//            public @NonNull Player parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//                Server server = (Server) context.getApplication().getServer();
//                CommandSender sender = (CommandSender) context.getCommandSender().getActualSender();
//                String argument = context.getCurrent();
//                Player player = server.getPlayer(argument);
//                if (player != null) {
//                    if (sender instanceof Player) {
//                        Player senderPlayer = (Player) sender;
//                        if (senderPlayer.canSee(player)) return player;
//                    } else return player;
//                }
//                throw new CommandExecutionException("error.player-not-found")
//                        .arguments(Placeholder.of("player", argument));
//            }
//
//            @Override
//            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
//                Server server = (Server) context.getApplication().getServer();
//                CommandSender sender = (CommandSender) context.getCommandSender().getActualSender();
//                final Collection<Player> players = new ArrayList<>(server.getOnlinePlayers());
//                if (sender instanceof Player) {
//                    Player senderPlayer = (Player) sender;
//                    players.removeIf(p -> !senderPlayer.canSee(p));
//                }
//                return players.stream().map(Player::getName).collect(Collectors.toList());
//            }
//
//        });
//        ArgumentParsers.register(OfflinePlayer.class, new OfflinePlayerArgumentParser());
//        ArgumentParsers.register(World.class, new ArgumentParser<>() {
//
//            @Override
//            public @NotNull World parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//                Server server = (Server) context.getApplication().getServer();
//                String argument = context.getCurrent();
//                World world = server.getWorld(argument);
//                if (world != null) return world;
//                else throw new CommandExecutionException("error.world-not-found")
//                        .arguments(Placeholder.of("world", argument));
//            }
//
//            @Override
//            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
//                Server server = (Server) context.getApplication().getServer();
//                return server.getWorlds().stream().map(World::getName).collect(Collectors.toList());
//            }
//
//        });
//        ArgumentParsers.register(Location.class, new ArgumentParser<>() {
//            private final @NotNull ArgumentParser<Double> xParser = new CoordinateParser(Location::getX);
//            private final @NotNull ArgumentParser<Double> yParser = new CoordinateParser(Location::getY);
//            private final @NotNull ArgumentParser<Double> zParser = new CoordinateParser(Location::getZ);
//
//            @Override
//            public @NotNull Location parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//                double x = xParser.parse(context);
//                if (context.isLast()) throw new CommandExecutionException("error.not-enough-arguments");
//                double y = yParser.parse(context.advanceCursor());
//                if (context.isLast()) throw new CommandExecutionException("error.not-enough-arguments");
//                double z = zParser.parse(context.advanceCursor());
//                return new Location(null, x, y, z);
//            }
//
//            @Override
//            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
//                return Collections.singletonList("<x> <y> <z>");
//            }
//
//            @Override
//            public boolean validateCompletions(final @NotNull CommandExecutionContext context) {
//                try {
//                    double x = xParser.parse(context);
//                    if (context.isLast()) return false;
//                    double y = yParser.parse(context.advanceCursor());
//                    if (context.isLast()) return false;
//                    double z = zParser.parse(context.advanceCursor());
//                    context.addParsedArgument(new Location(null, x, y, z));
//                    return !context.isLast();
//                } catch (CommandExecutionException e) {
//                    return false;
//                }
//            }
//
//        });
//    }
//
//    @Override
//    public @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application) {
//        return NMSUtils.getCommandDispatcher((Server) application.getServer())
//                .map(d -> new BrigadierBukkitCommandRegistry<>(application, d))
//                .map(r -> (CommandRegistry) r)
//                .orElse(new BukkitCommandRegistry(application));
//    }
//
//    @RequiredArgsConstructor
//    private static final class CoordinateParser implements ArgumentParser<Double> {
//        private static final @NotNull String currentIdentifier = "~";
//
//        private final @NotNull Function<Location, Double> coordinateGetter;
//        private final @NotNull ArgumentParser<Double> delegate = ArgumentParsers.of(Double.class);
//
//        @Override
//        public @NotNull Double parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//            String argument = context.getCurrent();
//            if (argument.startsWith(currentIdentifier)) {
//                Object sender = context.getCommandSender().getActualSender();
//                if (sender instanceof Player) {
//                    argument = argument.substring(currentIdentifier.length());
//                    double base = coordinateGetter.apply(((Player) sender).getLocation());
//                    if (!argument.isEmpty()) base += delegate.parse(context.setCurrent(argument));
//                    return base;
//                }
//            }
//            return Objects.requireNonNull(delegate.parse(context));
//        }
//
//        @Override
//        public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
//            List<String> completions = new ArrayList<>();
//            completions.addAll(delegate.getCompletions(context));
//            completions.add(currentIdentifier);
//            String argument = context.getCurrent();
//            if (argument.startsWith(currentIdentifier)) {
//                argument = argument.substring(currentIdentifier.length());
//                completions.addAll(delegate.getCompletions(context.setCurrent(argument)).stream()
//                        .map(d -> currentIdentifier + d)
//                        .collect(Collectors.toList()));
//            }
//            return completions;
//        }
//
//    }
//
//}
