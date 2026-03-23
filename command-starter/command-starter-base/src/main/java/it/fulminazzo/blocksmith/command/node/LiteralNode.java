package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A {@link CommandNode} to represent general literals.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class LiteralNode extends CommandNode {
    private final @NotNull String name;
    private final @NotNull Set<String> aliases;
    private @Nullable CommandInfo commandInfo;

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
    public Optional<CommandInfo> getCommandInfo() {
        return Optional.ofNullable(commandInfo);
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
    protected void validateInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        if (!hasPermission(context))
            throw new CommandExecutionException("error.no-permission")
                    .arguments(Placeholder.of("permission", getCommandInfo().orElseThrow().getPermission().getPermission()));
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return aliases.contains(token.trim().toLowerCase());
    }

    /**
     * Checks if the sender of the current context has permission to execute this node.
     *
     * @param context the context
     * @return <code>true</code> if they do
     */
    boolean hasPermission(final @NotNull CommandExecutionContext context) {
        return getCommandInfo()
                .map(CommandInfo::getPermission)
                .map(p -> context.getCommandSender().hasPermission(p))
                .orElse(true);
    }

}
