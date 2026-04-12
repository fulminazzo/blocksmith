package it.fulminazzo.blocksmith.command.node.info;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Carries information about a command.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class CommandInfo extends DataInfo<CommandInfo> {
    @NotNull String description;
    final @NotNull PermissionInfo permission;

    /**
     * Instantiates a new Command info.
     *
     * @param description the description
     * @param permission  the permission
     */
    public CommandInfo(final @NotNull String description, final @NotNull PermissionInfo permission) {
        this(description, permission, false);
    }

    /**
     * Instantiates a new Command info.
     *
     * @param description  the description
     * @param permission   the permission
     * @param autoComputed if <code>true</code>, will mark the permission as automatically computed,
     *                     meaning it was not provided by the user, but was instead retrieved from the command route
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    public CommandInfo(final @NotNull String description,
                       final @NotNull PermissionInfo permission,
                       final boolean autoComputed) {
        super(autoComputed);
        this.description = description;
        this.permission = permission;
    }

    @Override
    public void merge(final @NotNull CommandInfo commandInfo) {
        if (description.isEmpty() || isAutoComputed()) {
            description = commandInfo.getDescription();
            setAutoComputed(commandInfo.isAutoComputed());
        }
        permission.merge(commandInfo.getPermission());
    }

}
