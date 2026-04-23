package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
final class MockCommandRegistry extends CommandRegistry {
    @Getter
    private final @NotNull Map<String, CommandInfo> registeredCommands = new ConcurrentHashMap<>();

    public MockCommandRegistry() {
        super(new MockApplicationHandle());
    }

    public MockCommandRegistry(final @NotNull Messenger messenger, final @NotNull Logger logger) {
        super(new MockApplicationHandle(messenger, logger));
    }

    public @NotNull Map<String, LiteralNode> getCommands() {
        return Reflect.on(this).get("commands").get();
    }

    public void setState(final @NotNull State state) {
        Reflect.on(this).set("state", state);
    }

    public State getState() {
        return Reflect.on(this).get("state").get();
    }

    @Override
    public @NotNull CommandSenderWrapper<?> wrapSender(final @NotNull Object executor) {
        if (executor instanceof CommandSenderWrapper) return (CommandSenderWrapper<?>) executor;
        return new MockCommandSenderWrapper(application, (CommandSender) executor);
    }

    @Override
    protected void onRegister(final @NotNull String commandName,
                              final @NotNull LiteralNode command) {
        registeredCommands.put(commandName, command.getCommandInfo());
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        registeredCommands.remove(commandName);
    }

    @Override
    public @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

}
