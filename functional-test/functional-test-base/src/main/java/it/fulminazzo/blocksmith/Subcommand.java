package it.fulminazzo.blocksmith;

import org.jetbrains.annotations.NotNull;

/**
 * Identifies a subcommand.
 */
@FunctionalInterface
public interface Subcommand {

    /**
     * Executes the subcommand.
     *
     * @param executor  the executor
     * @param arguments the arguments
     */
    void execute(final @NotNull ExecutorWrapper executor, final @NotNull String @NotNull [] arguments);

}
