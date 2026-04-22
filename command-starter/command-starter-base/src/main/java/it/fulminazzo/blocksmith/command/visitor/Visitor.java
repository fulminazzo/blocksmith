package it.fulminazzo.blocksmith.command.visitor;

import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import org.jetbrains.annotations.NotNull;

/**
 * General object to visit {@link it.fulminazzo.blocksmith.command.node.CommandNode} objects.
 *
 * @param <T> the type of the result
 * @param <X> the type of the exception to throw in case of errors during visits
 */
public interface Visitor<T, X extends Exception> {

    /**
     * Visits a {@link ArgumentNode}.
     *
     * @param node the node
     * @return the result
     * @throws X the exception thrown in case of visit errors
     */
    T visitArgumentNode(final @NotNull ArgumentNode<?> node) throws X;

    /**
     * Visits a {@link LiteralNode}.
     *
     * @param node the node
     * @return the result
     * @throws X the exception thrown in case of visit errors
     */
    T visitLiteralNode(final @NotNull LiteralNode node) throws X;

}
