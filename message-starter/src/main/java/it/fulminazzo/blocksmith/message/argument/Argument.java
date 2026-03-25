package it.fulminazzo.blocksmith.message.argument;

import it.fulminazzo.blocksmith.message.MessageParseContext;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a general argument passed during the sending of a message.
 */
public interface Argument {

    /**
     * Applies the argument to the given component.
     *
     * @param context the context of action during parsing
     * @return the applied component
     */
    @NotNull Component apply(final @NotNull MessageParseContext context);

}
