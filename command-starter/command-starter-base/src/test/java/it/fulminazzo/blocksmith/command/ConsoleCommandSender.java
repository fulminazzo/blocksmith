package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;

public final class ConsoleCommandSender extends CommandSender {

    public ConsoleCommandSender() {
        super(ArgumentParsers.CONSOLE_COMMAND_NAME);
    }

}
