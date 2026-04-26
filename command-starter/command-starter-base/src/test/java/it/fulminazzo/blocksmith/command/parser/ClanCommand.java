package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.CommandSender;
import it.fulminazzo.blocksmith.command.annotation.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Command("(clan|team|gang)")
final class ClanCommand {

    @Command
    public void execute(
            final @NotNull CommandSender sender
    ) {
        // something
    }

    @Command(value = "(info|information|state) [name]", description = "Information command")
    @Async(1)
    public void getClanInfo(
            final @NotNull CommandSender sender,
            final @NotNull @Default("self") String clanName
    ) {
        // something
    }

    @Command("list <verbose>")
    @Permission(grant = Permission.Grant.ALL)
    public void list(
            final @NotNull CommandSender sender,
            final boolean verbose
    ) {
        // something
    }

    @Command("admin")
    public void admin() {
        // something
    }

    @Command("admin invite <target>")
    public void adminInvite(final @NotNull CommandSender sender,
                            final @NotNull Object target) {
        // something
    }

    @Command("admin members")
    @Permission(grant = Permission.Grant.ALL)
    public void adminMembers(final @NotNull CommandSender sender) {
        // something
    }

    @Command("admin members kick <target>")
    @Confirm(timeout = 20_000, unit = TimeUnit.MILLISECONDS, confirmAliases = "yes", cancelAliases = "no")
    public void adminMembersKick(final @NotNull CommandSender sender,
                                 final @NotNull @Tab("getMembers") Object target) {
        // something
    }

    @Command("admin gui edit")
    public void adminGuiEdit(final @NotNull CommandSender sender) {
        // something
    }

    public static void ignore(final @NotNull CommandSender sender) {
        // something
    }

    public @NotNull List<String> getMembers() {
        return List.of("player1", "player2");
    }

}
