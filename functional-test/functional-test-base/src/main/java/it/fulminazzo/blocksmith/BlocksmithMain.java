package it.fulminazzo.blocksmith;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Identifies the main entry point of a Blocksmith application.
 */
@RequiredArgsConstructor
public final class BlocksmithMain {
    private final @NotNull Map<String, Subcommand> commands = new ConcurrentHashMap<>();

    private final @NotNull Logger logger;

    @Getter
    private boolean enabled;

    /**
     * Enables the application.
     */
    public void enable() {
        if (isEnabled()) return;
        enabled = true;

        logger.info("Enabling...");
        logger.info("┏┓ ╻  ┏━┓┏━╸╻┏ ┏━┓┏┳┓╻╺┳╸╻ ╻");
        logger.info("┣┻┓┃  ┃ ┃┃  ┣┻┓┗━┓┃┃┃┃ ┃ ┣━┫");
        logger.info("┗━┛┗━╸┗━┛┗━╸╹ ╹┗━┛╹ ╹╹ ╹ ╹ ╹");

        // Commands
        registerCommand("hello", (e, a) -> e.sendMessage("world!"));

        logger.info("Successfully enabled. Welcome!");
    }

    /**
     * Disables the application.
     */
    public void disable() {
        if (!isEnabled()) return;
        enabled = false;
        logger.info("Disabling...");
        logger.info("Successfully disabled. Goodbye");
    }

    /**
     * Executes the command.
     *
     * @param executor  the executor
     * @param command   the command name
     * @param arguments the arguments of the command
     */
    public void executeCommand(
            final @NotNull ExecutorWrapper executor,
            final @NotNull String command,
            final @NotNull String @NotNull [] arguments
    ) {
        for (Map.Entry<String, Subcommand> entry : commands.entrySet())
            if (entry.getKey().equalsIgnoreCase(command)) {
                entry.getValue().execute(executor, arguments);
                return;
            }
        executor.sendMessage("Could not find command: " + command);
    }

    /**
     * Registers a command.
     *
     * @param command  the command
     * @param executor the executor
     */
    public void registerCommand(final @NotNull String command, final @NotNull Subcommand executor) {
        commands.put(command, executor);
    }

    /**
     * Gets the commands.
     *
     * @return the commands
     */
    public @NotNull Set<String> getCommands() {
        return commands.keySet();
    }

}
