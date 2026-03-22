package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.annotation.Command;
import it.fulminazzo.blocksmith.command.annotation.Permission;

@SuppressWarnings("unused")
@Command(value = "(clan|team|gang)", description = "Clan base command")
@Permission(value = "blocksmith.edited.clan", permissionDefault = Permission.Default.ALL)
final class ClanCommand {

    @Command
    public void execute() {
        // something
    }

    @Command(value = "admin", description = "Clan admin command")
    public void admin() {
        // something
    }

}
