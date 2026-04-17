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

import java.util.Collections;
import java.util.List;

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

    @Override
    public @NotNull List<String> visitArgumentNode(final @NotNull ArgumentNode<?> node) {
        if (!input.isLast())
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
        if (!input.isLast()) {
            if (!node.getAliases().contains(input.getCurrent().toLowerCase()) ||
                    commandSender.hasPermission(node.getCommandInfo().getPermission()))
                return Collections.emptyList();
            input.advanceCursor();
        }
        return visitCommandNode(node);
    }

    @Override
    protected @NotNull List<String> visitCommandNode(final @NotNull CommandNode node) {
        //TODO: implement
        throw new UnsupportedOperationException();
    }

}
