package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.node.CommandInfo;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.message.Messenger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
final class MockCommandRegistry extends CommandRegistry {
    @Getter
    private final @NotNull Map<String, CommandInfo> registeredCommands = new ConcurrentHashMap<>();

    public MockCommandRegistry() {
        this(new Messenger(log), log);
    }

    public MockCommandRegistry(final @NotNull Messenger messenger, final @NotNull Logger logger) {
        super(messenger, logger);
    }

    @SuppressWarnings("unchecked")
    public @NotNull Map<String, LiteralNode> getCommands() {
        try {
            Class<?> clazz = CommandRegistry.class;
            Field field = clazz.getDeclaredField("commands");
            field.setAccessible(true);
            return (Map<String, LiteralNode>) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setState(final @NotNull State state) {
        try {
            Class<?> clazz = CommandRegistry.class;
            Field field = clazz.getDeclaredField("state");
            field.setAccessible(true);
            field.set(this, state);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public State getState() {
        try {
            Class<?> clazz = CommandRegistry.class;
            Field field = clazz.getDeclaredField("state");
            field.setAccessible(true);
            return (State) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected @NotNull CommandSenderWrapper wrapSender(final @NotNull Object executor) {
        return new MockCommandSenderWrapper((CommandSender) executor);
    }

    @Override
    protected void onRegister(final @NotNull String commandName,
                              final @NotNull LiteralNode command) {
        registeredCommands.put(commandName, command.getCommandInfo().orElseThrow());
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        registeredCommands.remove(commandName);
    }

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

    @Override
    protected @NotNull String getPrefix() {
        return "blocksmith";
    }

}
