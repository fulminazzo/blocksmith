package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * Abstraction of {@link InjectedNode} to generate an injected node
 * with description and permission computed from the parent node if not present.
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
abstract class InjectedNode extends LiteralNode {
    private static final String DESCRIPTION_SUFFIX = ".description";

    /**
     * Instantiates a new Injected node.
     *
     * @param aliases     the aliases
     * @param description the description
     * @param permission  the permission
     * @param parent      the parent node
     */
    protected InjectedNode(final @NotNull String @NotNull [] aliases,
                           @NotNull String description,
                           @NotNull Permission permission,
                           final @NotNull LiteralNode parent) {
        super(aliases);
        final CommandInfo reference = parent.getCommandInfo();
        final String identifier = "." + getName();
        if (description.isEmpty()) description = reference.getDescription()
                .replace(DESCRIPTION_SUFFIX, identifier + DESCRIPTION_SUFFIX);
        PermissionInfo referencePermission = reference.getPermission();
        final String prefix;
        final String actualPermission;
        final Permission.Grant grant;
        if (permission.value().isEmpty()) {
            prefix = referencePermission.getPrefix();
            actualPermission = referencePermission.getActualPermission() + identifier;
        } else {
            prefix = permission.group();
            actualPermission = permission.value();
        }
        grant = permission.grant() == Permission.Grant.OP ? referencePermission.getGrant() : permission.grant();
        setCommandInfo(new CommandInfo(
                description,
                new PermissionInfo(prefix, actualPermission, grant)
        ));
    }

}
