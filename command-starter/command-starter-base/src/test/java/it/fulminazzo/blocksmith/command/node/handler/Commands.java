package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.CommandSender;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.ConsoleCommandSender;
import it.fulminazzo.blocksmith.command.Player;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
class Commands {

    public void sendPlayerOnlyWrapper(final @NotNull CommandSenderWrapper<Player> sender,
                                      final @NotNull String message) {
        // send message to wrapped player
    }

    public void sendConsoleOnlyWrapper(final @NotNull CommandSenderWrapper<ConsoleCommandSender> sender,
                                       final @NotNull String message) {
        // send message to wrapped console
    }

    public void sendPlayerOnly(final @NotNull Player sender, final @NotNull String message) {
        // send message to player
    }

    public void sendConsoleOnly(final @NotNull ConsoleCommandSender sender, final @NotNull String message) {
        // send message to console
    }

    public void send(final @NotNull CommandSender sender, final @NotNull String message) {
        // send message to sender
    }

    public void broadcast(final @NotNull String message) {
        // broadcast logic
    }

    public void executionException(final @NotNull String message) throws CommandExecutionException {
        throw new CommandExecutionException("Execution exception!");
    }

    public void runtimeException(final @NotNull String message) {
        throw new RuntimeException("Commands have not been initialized!");
    }

    public void exception(final @NotNull String message) throws Exception {
        throw new Exception("Commands have not been initialized!");
    }

    public void unknown(final @NotNull String message) {
        throw new RuntimeException();
    }

}
