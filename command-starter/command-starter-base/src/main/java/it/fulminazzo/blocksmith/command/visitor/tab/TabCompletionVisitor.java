package it.fulminazzo.blocksmith.command.visitor.tab;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.visitor.VisitorImpl;
import it.fulminazzo.blocksmith.validation.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A special {@link it.fulminazzo.blocksmith.command.visitor.Visitor} handling tab completion.
 * <br>
 * Uses {@link RuntimeException} as exception because it should never actually throw anything.
 */
public final class TabCompletionVisitor extends VisitorImpl<@NotNull List<String>, RuntimeException> {

    /**
     * Instantiates a new Tab completion visitor.
     *
     * @param application   the application
     * @param commandSender the command sender
     * @param commandName   the command name
     * @param arguments     the arguments
     */
    public TabCompletionVisitor(final @NotNull ApplicationHandle application,
                                final @NotNull CommandSenderWrapper<?> commandSender,
                                final @NotNull String commandName,
                                final @NotNull String @NotNull ... arguments) {
        super(application, commandSender, commandName, arguments);
    }

    /**
     * Gets the appropriate tab completions for the given node.
     *
     * @param node the node
     * @return the completions
     */
    public @NotNull List<String> tabComplete(final @NotNull CommandNode node) {
        return node.accept(this);
    }

    @Override
    public @NotNull List<String> visitArgumentNode(final @NotNull ArgumentNode<?> node) {
        if (input.isLast()) return filterCompletions(node.getCompletions(this));
        try {
            node.parseCurrent(this);
            input.advanceCursor();
        } catch (ArgumentParseException | ValidationException e) {
            return Collections.emptyList();
        }
        return visitCommandNode(node);
    }

    @Override
    public @NotNull List<String> visitLiteralNode(final @NotNull LiteralNode node) {
        if (!input.isDone() && !node.getAliases().contains(input.getCurrent().toLowerCase()) ||
                !commandSender.hasPermission(node.getCommandInfo().getPermission()))
            return Collections.emptyList();
        if (input.isLast()) return filterCompletions(node.getCompletions(this));
        input.advanceCursor();
        return visitCommandNode(node);
    }

    @Override
    protected @NotNull List<String> visitCommandNode(final @NotNull CommandNode node) {
        if (input.isLast()) {
            return filterCompletions(node.getChildren().stream()
                    .map(c -> c.getCompletions(this))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        } else {
            if (input.isDone()) {
                if (node instanceof ArgumentNode<?> && ((ArgumentNode<?>) node).isGreedy())
                    return node.getCompletions(this);
                else return Collections.emptyList();
            }
            final String current = input.getCurrent();
            CommandNode child = node.getChild(current);
            if (child == null) return Collections.emptyList();
            else return child.accept(this);
        }
    }

    private @NotNull List<String> filterCompletions(final @NotNull List<String> completions) {
        List<String> filtered = completions.stream()
                .filter(c -> c.toLowerCase().startsWith(input.getCurrent().toLowerCase()))
                .collect(Collectors.toList());
        if (filtered.isEmpty())
            return completions.stream()
                    .filter(c -> c.startsWith("<"))
                    .collect(Collectors.toList());
        else return filtered;
    }

}
