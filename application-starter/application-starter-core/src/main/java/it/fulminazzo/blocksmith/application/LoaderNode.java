package it.fulminazzo.blocksmith.application;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Identifies a general node in the dependency graph.
 *
 * @see FieldAnnotationNode
 */
@Data
class LoaderNode {
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final @NotNull Set<LoaderNode> children = new LinkedHashSet<>();

    /**
     * Adds a child node to this node.
     *
     * @param node the child node to add
     */
    public void addChild(final @NotNull LoaderNode node) {
        children.add(node);
    }

}
