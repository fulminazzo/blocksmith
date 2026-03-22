package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.CommandSender;
import it.fulminazzo.blocksmith.command.annotation.Command;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
final class GeneralCommands {

    @Command(value = "help [command]", description = "Displays help for all the available commands")
    @Permission(value = "blocksmith.help", permissionDefault = Permission.Default.ALL)
    public static void help(final @NotNull CommandSender sender,
                            final @Nullable String command
    ) {
    }

    @Command("reload plugin <async>")
    public static void reload(boolean async) {
    }

}
