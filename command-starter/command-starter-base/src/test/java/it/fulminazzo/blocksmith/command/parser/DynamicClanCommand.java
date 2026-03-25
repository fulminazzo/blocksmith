package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.CommandSender;
import it.fulminazzo.blocksmith.command.annotation.Command;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
@Command(dynamic = true)
final class DynamicClanCommand {

    @Command(value = "<value>")
    public void execute(
            final @NotNull CommandSender sender,
            final double value
    ) {
        // something
    }

    @Command("help <verbose>")
    @Permission(grant = Permission.Grant.ALL)
    public void help(
            final @NotNull CommandSender sender,
            final boolean verbose
    ) {
        // something
    }

    public @NotNull List<String> getAliases() {
        return Arrays.asList("clan", "gang", "team");
    }

}
