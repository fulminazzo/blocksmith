package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.CommandSender;
import it.fulminazzo.blocksmith.command.annotation.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
final class DynamicGeneralCommands {

    @Command(dynamic = true)
    public static void help(final @NotNull CommandSender sender) {
    }

    public static @NotNull List<String> getHelpAliases() {
        return Arrays.asList("help", "?");
    }

}
