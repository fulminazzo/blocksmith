package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.CommandSender;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.annotation.Command;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
final class DynamicGeneralCommands {

    @Command(dynamic = true)
    @Permission(value = "help", grant = Permission.Grant.NONE, group = "plugin")
    public static void help(final @NotNull CommandSenderWrapper<CommandSender> sender) {
    }

    public static @NotNull List<String> getHelpAliases() {
        return Arrays.asList("help", "?");
    }

}
