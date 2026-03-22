package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.CommandInfo;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.PermissionInfo;
import it.fulminazzo.blocksmith.command.parser.CommandParser;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles registration of commands.
 */
@RequiredArgsConstructor
public abstract class CommandRegistry {
    private final @NotNull Map<String, LiteralNode> commands = new ConcurrentHashMap<>();
    private @NotNull State state = State.INITIAL;

    /**
     * Allows to dynamically insert new commands after a {@link #commit()} call.
     * <br>
     * <b>WARNING</b>: this method has not been tested in every platform.
     * As such, it might cause some unexpected behavior.
     *
     * @param commandModules the command modules
     * @return this object (for method chaining)
     */
    public final @NotNull CommandRegistry insert(final Object @NotNull ... commandModules) {
        if (state != State.REGISTERED)
            throw new IllegalStateException(String.format("This method is only available after registration. " +
                    "Please call %s#commit() before using it", getClass().getSimpleName()));
        Map<String, LiteralNode> nodes = getCommandNodes(commandModules);
        for (LiteralNode node : nodes.values()) {
            String name = node.getName();
            if (commands.containsKey(name))
                throw new IllegalArgumentException(String.format("Could not add command '%s' as it has already been registered", name));
        }
        for (String name : nodes.keySet()) {
            LiteralNode node = nodes.get(name);
            commands.put(name, node);
            registerSingle(name, node);
        }
        return this;
    }

    /**
     * Extracts all the methods annotated with {@link it.fulminazzo.blocksmith.command.annotation.Command}
     * from the given modules and adds them to the registration pool.
     * <br>
     * <b>WARNING</b>: despite its name, calling this method will <b>NOT</b> be enough to register the commands.
     * It is required to call {@link #commit()} to make the changes.
     *
     * @param commandModules the command modules
     * @return this object (for method chaining)
     */
    public final @NotNull CommandRegistry register(final Object @NotNull ... commandModules) {
        if (state == State.REGISTERED)
            throw new IllegalStateException("It is not possible to register new commands at this time. Please register all commands before committing");
        state = State.REGISTERING;
        for (LiteralNode node : getCommandNodes(commandModules).values())
            commands.merge(node.getName(), node, LiteralNode::merge);
        return this;
    }

    private @NotNull Map<String, LiteralNode> getCommandNodes(final Object @NotNull ... commandModules) {
        List<CommandNode> nodes = new ArrayList<>();
        for (Object commandModule : commandModules)
            nodes.addAll(CommandParser.parseCommands(commandModule, getSenderType(), getPrefix()));
        Map<String, LiteralNode> map = new HashMap<>();
        for (CommandNode node : nodes)
            commands.merge(node.getName(), (LiteralNode) node, LiteralNode::merge);
        return map;
    }

    /**
     * Registers all the commands previously extracted from {@link #register(Object...)}.
     *
     * @return this object (for method chaining)
     */
    public final @NotNull CommandRegistry commit() {
        if (state == State.REGISTERED)
            throw new IllegalStateException("Commands have already been registered");
        else if (state != State.REGISTERING)
            throw new IllegalStateException("No commands has been registered at this time");
        state = State.REGISTERED;
        commands.forEach(this::registerSingle);
        return this;
    }

    @SuppressWarnings("deprecation")
    private void registerSingle(final @NotNull String commandName,
                                final @NotNull LiteralNode commandNode) {
        onRegister(commandName, commandNode.getCommandInfo().orElse(new CommandInfo(
                CommandParser.getDefaultDescription(commandName),
                new PermissionInfo(getPrefix() + "." + commandName, Permission.Default.OP, true)
        )));
    }

    /**
     * Unregisters all the commands.
     */
    public final void unregisterAll() {
        if (state != State.REGISTERED)
            throw new IllegalStateException(String.format("Commands have not been registered yet. " +
                    "Did you forget to call %s#commit()?", getClass().getSimpleName()));
        state = State.INITIAL;
        new HashSet<>(commands.keySet()).forEach(this::unregister);
    }

    /**
     * Unregisters the given command.
     *
     * @param commandName the command name
     */
    protected final void unregister(final @NotNull String commandName) {
        if (commands.remove(commandName) != null) onUnregister(commandName);
    }

    /**
     * Method called upon actively registering a command.
     *
     * @param commandName the command name
     * @param commandInfo the command info
     */
    protected abstract void onRegister(final @NotNull String commandName,
                                       final @NotNull CommandInfo commandInfo);

    /**
     * Method called upon unregistering a command.
     *
     * @param commandName the command name
     */
    protected abstract void onUnregister(final @NotNull String commandName);

    /**
     * Gets the sender type.
     *
     * @return the sender type
     */
    protected abstract @NotNull Class<?> getSenderType();

    /**
     * Gets the prefix to prepend to automatically computed permissions.
     *
     * @return the prefix
     */
    protected abstract @NotNull String getPrefix();

    /**
     * Identifies the state of the current registry.
     */
    enum State {
        /**
         * The registry has just been created and nothing has been used yet.
         */
        INITIAL,
        /**
         * Commands have been added to the registration pool, but they have not been registered yet.
         */
        REGISTERING,
        /**
         * Commands have been successfully registered and it is not possible to register new commands.
         */
        REGISTERED

    }

}
