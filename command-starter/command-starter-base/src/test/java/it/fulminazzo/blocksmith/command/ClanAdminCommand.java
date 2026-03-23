package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.annotation.Command;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Command("clan")
public class ClanAdminCommand {

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
    public void adminMembersKick(final @NotNull CommandSender sender,
                                 final @NotNull Object target) {
        // something
    }

}
