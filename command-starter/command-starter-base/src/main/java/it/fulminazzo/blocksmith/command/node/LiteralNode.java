package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.annotation.Confirm;
import it.fulminazzo.blocksmith.command.node.handler.ConfirmationHandler;
import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a static node in a command tree.
 * Can have multiple aliases.
 */
@Getter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class LiteralNode extends CommandNode {
    final @NotNull String name;
    final @NotNull Set<String> aliases;

    @Setter
    @Nullable CommandInfo commandInfo;

    @Nullable ConfirmationHandler confirmationHandler;

    /**
     * Instantiates a new Literal node.
     *
     * @param literals the literals
     */
    public LiteralNode(final String @NotNull ... literals) {
        if (literals.length == 0) throw new IllegalArgumentException("At least one literal must be provided");
        this.name = literals[0].trim().toLowerCase();
        this.aliases = new HashSet<>();
        for (String literal : literals) this.aliases.add(literal.trim().toLowerCase());
    }

    /**
     * Gets the command information.
     *
     * @return the command info
     * @throws IllegalStateException if the command information was never set (use {@link #setCommandInfo(CommandInfo)})
     */
    public @NotNull CommandInfo getCommandInfo() {
        if (commandInfo == null)
            throw new IllegalStateException("Literal node not correctly initialized, missing command information: " + this);
        return commandInfo;
    }

    /**
     * Toggles requirements for confirmation to execute this node.
     *
     * @param confirmationInfo the confirmation info ({@code null} to disable)
     * @return this object (for method chaining)
     */
    public @NotNull LiteralNode setConfirmationInfo(final @Nullable Confirm confirmationInfo) {
        if (confirmationInfo == null) this.confirmationHandler = null;
        else this.confirmationHandler = new ConfirmationHandler(confirmationInfo);
        return this;
    }

    @Override
    public @NotNull LiteralNode merge(final @NotNull CommandNode node) {
        if (node instanceof LiteralNode) {
            LiteralNode literalNode = (LiteralNode) node;
            aliases.addAll(literalNode.aliases);
            CommandInfo i = literalNode.getCommandInfo();
            if (commandInfo == null) setCommandInfo(i);
            else commandInfo.merge(i);
        }
        return (LiteralNode) super.merge(node);
    }

    @Override
    public <T, X extends Exception> T accept(final @NotNull Visitor<T, X> visitor) throws X {
        return visitor.visitLiteralNode(this);
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return aliases.contains(token.trim().toLowerCase());
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        @NotNull CommandSenderWrapper<?> sender = visitor.getCommandSender();
        final List<String> completions = new ArrayList<>();
        if (sender.hasPermission(getCommandInfo().getPermission())) {
            if (confirmationHandler != null)
                completions.addAll(confirmationHandler.getCompletions(visitor));
            completions.addAll(aliases);
        }
        return completions;
    }

}
