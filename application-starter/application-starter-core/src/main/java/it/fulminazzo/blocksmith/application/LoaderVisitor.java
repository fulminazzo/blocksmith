package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.application.node.FieldAnnotationNode;
import it.fulminazzo.blocksmith.application.node.LoaderNode;
import it.fulminazzo.blocksmith.application.node.RootLoaderNode;
import org.jetbrains.annotations.NotNull;

/**
 * Identifies a visitor for a {@link LoaderNode} dependency tree.
 *
 * @param <X> the type of the exception to throw in case of errors during visits
 * @see ApplicationLoader
 * @see LoaderNode
 * @see RootLoaderNode
 * @see FieldAnnotationNode
 */
public interface LoaderVisitor<X extends Throwable> {

    /**
     * Visits a general {@link FieldAnnotationNode}.
     *
     * @param node the node
     * @throws X the exception thrown in case of visit errors
     */
    void visitField(final @NotNull FieldAnnotationNode node) throws X;

    /**
     * Visits a general {@link RootLoaderNode}.
     *
     * @param node the node
     * @throws X the exception thrown in case of visit errors
     */
    void visitRoot(final @NotNull RootLoaderNode node) throws X;

}
