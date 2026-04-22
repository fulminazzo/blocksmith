package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
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
     * @param visitor the visitor requesting the completions
     * @return the completions
     */
    @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor);

}
