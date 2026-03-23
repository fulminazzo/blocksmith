package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an object that returns a list of tab completions.
 */
@FunctionalInterface
public interface TabCompletable {

    /**
     * Gets the tab completions.
     *
     * @param context the current context of action
     * @return the completions
     */
    @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context);

}
