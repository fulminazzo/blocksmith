package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * Abstraction of {@link CommandNode} for confirmation related nodes.
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
abstract class ConfirmationNode extends LiteralNode {

    /**
     * Instantiates a new Confirmation node.
     *
     * @param aliases     the aliases
     * @param description the description
     * @param permission  the permission
     * @param parent      the parent node
     */
    protected ConfirmationNode(final @NotNull String @NotNull [] aliases,
                               @NotNull String description,
                               @NotNull String permission,
                               final @NotNull LiteralNode parent) {
        super(aliases);
        final CommandInfo reference = parent.getCommandInfo();
        if (description.isEmpty()) description = reference.getDescription() + "." + getName();
        PermissionInfo referencePermission = reference.getPermission();
        if (permission.isEmpty()) permission = referencePermission.getActualPermission() + "." + getName();
        setCommandInfo(new CommandInfo(
                description,
                new PermissionInfo(referencePermission.getPrefix(), permission, referencePermission.getGrant())
        ));
    }

}
