package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.CommandSender;
import it.fulminazzo.blocksmith.command.annotation.Command;
import it.fulminazzo.blocksmith.command.annotation.Default;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Command("(clan|team|gang)")
public final class ClanCommand {

    @Command(value = "(info|information|state) [name]", description = "Information command")
    public void getClanInfo(
            final @NotNull CommandSender sender,
            final @NotNull @Default("self") String clanName
    ) {
        // something
    }

    @Command("help <verbose>")
    @Permission(permissionDefault = Permission.Default.ALL)
    public void help(
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
    @Permission(permissionDefault = Permission.Default.ALL)
    public void adminMembers(final @NotNull CommandSender sender) {
        // something
    }

    @Command("admin members kick <target>")
    public void adminMembersKick(final @NotNull CommandSender sender,
                                 final @NotNull Object target) {
        // something
    }

    public static void ignore(final @NotNull CommandSender sender) {
        // something
    }

}
