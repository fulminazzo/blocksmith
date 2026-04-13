package it.fulminazzo.blocksmith.command.visitor;

import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.NumberArgumentNode;
import org.jetbrains.annotations.NotNull;

/**
 * General object to visit {@link it.fulminazzo.blocksmith.command.node.CommandNode} objects.
 *
 * @param <T> the type of the result
 */
public interface Visitor<T> {

    /**
     * Visits a {@link ArgumentNode}.
     *
     * @param node the node
     * @return the result
     */
    T visitArgumentNode(final @NotNull ArgumentNode<?> node);

    /**
     * Visits a {@link NumberArgumentNode}.
     *
     * @param node the node
     * @return the result
     */
    T visitNumberArgumentNode(final @NotNull NumberArgumentNode<?> node);

    /**
     * Visits a {@link LiteralNode}.
     *
     * @param node the node
     * @return the result
     */
    T visitLiteralNode(final @NotNull LiteralNode node);

}
