package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Confirm;
import it.fulminazzo.blocksmith.command.node.handler.ConfirmationHandler;
import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a static node in a command tree.
 * Can have multiple aliases.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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
     * If this literal represents the final command (or subcommand) of a command route,
     * its command information will be available.
     *
     * @return the command info, if available
     */
    public @NotNull Optional<CommandInfo> getCommandInfo() {
        return Optional.ofNullable(commandInfo);
    }

    /**
     * Toggles requirements for confirmation to execute this node.
     *
     * @param confirmationInfo the confirmation info (<code>null</code> to disable)
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
            literalNode.getCommandInfo().ifPresent(i -> {
                if (commandInfo == null) setCommandInfo(i);
                else commandInfo.merge(i);
            });
        }
        return (LiteralNode) super.merge(node);
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return aliases.contains(token.trim().toLowerCase());
    }

}
