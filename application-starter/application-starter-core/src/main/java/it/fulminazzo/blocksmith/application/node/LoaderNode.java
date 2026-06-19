package it.fulminazzo.blocksmith.application.node;

import it.fulminazzo.blocksmith.application.LoaderVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Identifies a general node in the dependency graph.
 *
 * @see RootLoaderNode
 * @see FieldAnnotationNode
 */
@Data
public abstract class LoaderNode {
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final @NotNull Set<LoaderNode> children = new LinkedHashSet<>();

    /**
     * Inspects this node with the given visitor.
     *
     * @param visitor the visitor
     */
    public abstract void accept(final @NotNull LoaderVisitor visitor);

    /**
     * Adds a child node to this node.
     *
     * @param node the child node to add
     */
    public void addChild(final @NotNull LoaderNode node) {
        children.add(node);
    }

}
