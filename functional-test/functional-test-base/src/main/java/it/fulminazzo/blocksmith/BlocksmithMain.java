package it.fulminazzo.blocksmith;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Identifies the main entry point of a Blocksmith application.
 */
@RequiredArgsConstructor
public final class BlocksmithMain {
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
        // logic
    }

}
