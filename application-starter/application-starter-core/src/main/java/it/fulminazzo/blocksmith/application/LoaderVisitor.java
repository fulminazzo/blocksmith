package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.application.node.FieldAnnotationNode;
import it.fulminazzo.blocksmith.application.node.LoaderNode;
import it.fulminazzo.blocksmith.application.node.RootLoaderNode;
import org.jetbrains.annotations.NotNull;

/**
 * Identifies a visitor for a {@link LoaderNode} dependency tree.
 *
 * @see ApplicationLoader
 * @see LoaderNode
 * @see RootLoaderNode
 * @see FieldAnnotationNode
 */
public interface LoaderVisitor {

    /**
     * Visits a general {@link FieldAnnotationNode}.
     *
     * @param node the node
     */
    void visitField(final @NotNull FieldAnnotationNode node);

    /**
     * Visits a general {@link RootLoaderNode}.
     *
     * @param node the node
     */
    void visitRoot(final @NotNull RootLoaderNode node);

}
