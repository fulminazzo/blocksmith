package it.fulminazzo.blocksmith.message.argument;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a general argument passed during the sending of a message.
 */
public interface Argument {

    /**
     * Applies the argument to the given component.
     *
     * @param component the component
     * @return the applied component
     */
    @NotNull Component apply(final @NotNull Component component);

}
