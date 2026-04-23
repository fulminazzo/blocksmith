package it.fulminazzo.blocksmith.command;

public final class ConsoleCommandSender extends CommandSender {

    public ConsoleCommandSender() {
        super(CommandSenderWrapper.CONSOLE_COMMAND_NAME);
    }

}
