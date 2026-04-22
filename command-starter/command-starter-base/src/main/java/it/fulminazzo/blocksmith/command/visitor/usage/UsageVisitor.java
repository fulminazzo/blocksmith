package it.fulminazzo.blocksmith.command.visitor.usage;

import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A visitor for generating the usage of a {@link it.fulminazzo.blocksmith.command.node.CommandNode}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UsageVisitor implements Visitor<@NotNull String, RuntimeException> {
    private static final @NotNull UsageVisitor INSTANCE = new UsageVisitor();

    private static final @NotNull String COMMAND_START = "/";

    private final @NotNull Visitor<@NotNull String, RuntimeException> singleUsageVisitor = new SimpleUsageVisitor();

    @Override
    public @NotNull String visitArgumentNode(final @NotNull ArgumentNode<?> node) {
        return visitCommandNode(node);
    }

    @Override
    public @NotNull String visitLiteralNode(final @NotNull LiteralNode node) {
        return visitCommandNode(node);
    }

    @Override
    public @NotNull String visitCommandNode(final @NotNull CommandNode node) {
        final UsageStyle style = UsageStyle.get();
        return UsageStyle.colorize(COMMAND_START, style.getLiteralColor()) +
                visitParentNode(node) +
                node.accept(singleUsageVisitor) +
                visitChildren(node);
    }

    /**
     * Visits the parent nodes of a node and returns the usage of the parents.
     *
     * @param node the node
     * @return the usage of the parents
     */
    @NotNull String visitParentNode(final @NotNull CommandNode node) {
        final StringBuilder usage = new StringBuilder();
        CommandNode parent = node.getParent();
        while (parent != null) {
            usage.insert(0, parent.accept(singleUsageVisitor) + " ");
            parent = parent.getParent();
        }
        return usage.toString();
    }

    /**
     * Visits the children of a node and returns the usages of the children.
     *
     * @param node the node
     * @return the usages of the children
     */
    @NotNull String visitChildren(final @NotNull CommandNode node) {
        final UsageStyle style = UsageStyle.get();
        final StringBuilder usage = new StringBuilder();
        CommandNode current = node;
        Set<CommandNode> children;
        while (!(children = current.getChildren()).isEmpty()) {
            if (children.size() == 1) {
                current = children.stream().findFirst().orElseThrow();
                usage.append(" ").append(current.accept(singleUsageVisitor));
            } else {
                usage.append(" ").append(children.stream()
                        .map(c -> c.accept(singleUsageVisitor))
                        .collect(Collectors.joining(
                                UsageStyle.colorize(style.getSeparator(), style.getPunctuationColor())
                        ))
                );
                break;
            }
        }
        return usage.toString();
    }

    /**
     * Generates the usage of the given command node.
     *
     * @param node the command node
     * @return the usage of the command node
     */
    public static @NotNull String generateUsage(final @NotNull CommandNode node) {
        return INSTANCE.visitCommandNode(node);
    }

    /**
     * A basic visitor for generating the usage literal of a single node.
     */
    static final class SimpleUsageVisitor implements Visitor<@NotNull String, RuntimeException> {

        @Override
        public @NotNull String visitArgumentNode(final @NotNull ArgumentNode<?> node) {
            UsageStyle style = UsageStyle.get();
            Class<?> type = node.getType();
            final String format = node.isOptional()
                    ? style.getOptionalArgumentFormat()
                    : style.getArgumentFormat();
            final String color = node.isOptional()
                    ? style.getOptionalArgumentColor(type)
                    : style.getArgumentColor(type);
            final String name = node.isGreedy()
                    ? String.format(style.getGreedyArgumentFormat(), node.getName())
                    : node.getName();
            return String.format(format, UsageStyle.colorize(name, color));
        }

        @Override
        public @NotNull String visitLiteralNode(final @NotNull LiteralNode node) {
            UsageStyle style = UsageStyle.get();
            return node.getAliases().stream()
                    .map(a -> UsageStyle.colorize(a, style.getLiteralColor()))
                    .sorted()
                    .collect(Collectors.joining(
                            UsageStyle.colorize(style.getLiteralSeparator(), style.getLiteralSeparatorColor())
                    ));
        }

        @Override
        public String visitCommandNode(final @NotNull CommandNode node) {
            throw new UnsupportedOperationException();
        }

    }

}
