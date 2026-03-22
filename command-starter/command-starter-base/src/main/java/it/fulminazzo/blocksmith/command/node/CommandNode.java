package it.fulminazzo.blocksmith.command.node;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the node of a command, whether it is static or dynamic.
 */
@EqualsAndHashCode
@ToString
public abstract class CommandNode {
    @Getter
    private final @NotNull Set<CommandNode> children = new TreeSet<>(Comparator.comparing(CommandNode::getName));
    @Setter
    private @Nullable ExecutionInfo executionInfo;

    /**
     * Checks if the node matches with the given token.
     *
     * @param token the token
     * @return <code>true</code> if it does
     */
    public abstract boolean matches(final @NotNull String token);

    /**
     * Gets the name of this node.
     *
     * @return the name
     */
    public abstract @NotNull String getName();

    /**
     * Adds a new child to this node.
     *
     * @param child the child
     * @return the added child (if it was already present, it might be returned updated)
     */
    public @NotNull CommandNode addChild(final @NotNull CommandNode child) {
        for (CommandNode c : children)
            if (child.getClass().equals(c.getClass()) && c.getName().equals(child.getName())) {
                c.merge(child);
                return c;
            }
        children.add(child);
        return child;
    }

    /**
     * Gets the execution info.
     *
     * @return the execution info, if available
     */
    public Optional<ExecutionInfo> getExecutionInfo() {
        return Optional.ofNullable(executionInfo);
    }

    /**
     * Checks if the current node is executable.
     *
     * @return <code>true</code> if it is
     */
    public boolean isExecutable() {
        return executionInfo != null;
    }

    /**
     * Merges the given node data with the current one.
     *
     * @param node the node
     */
    void merge(final @NotNull CommandNode node) {
        node.getChildren().forEach(this::addChild);
        if (executionInfo == null) node.getExecutionInfo().ifPresent(this::setExecutionInfo);
    }

}
