package it.fulminazzo.blocksmith.command.visitor.usage;

import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class UsageVisitor {


    /**
     * A basic visitor for generating the usage literal of a single node.
     */
    static final class SimpleUsageVisitor implements Visitor<String, RuntimeException> {

        @Override
        public String visitArgumentNode(final @NotNull ArgumentNode<?> node) {
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
        public String visitLiteralNode(final @NotNull LiteralNode node) {
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
