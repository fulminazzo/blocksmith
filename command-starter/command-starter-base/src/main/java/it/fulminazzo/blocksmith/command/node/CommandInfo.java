package it.fulminazzo.blocksmith.command.node;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the command information.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter(AccessLevel.NONE)
public final class CommandInfo {
    @NotNull String description;
    final @NotNull PermissionInfo permission;

    @Getter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    boolean autoComputed;

    /**
     * Instantiates a new Command info.
     *
     * @param description the description
     * @param permission  the permission
     */
    public CommandInfo(final @NotNull String description,
                       final @NotNull PermissionInfo permission) {
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
        this.description = description;
        this.permission = permission;
        this.autoComputed = autoComputed;
    }

    /**
     * Merges the given command information into the current one
     * (only if the information are not given).
     *
     * @param commandInfo the command info to merge from
     */
    public void merge(final @NotNull CommandInfo commandInfo) {
        if (description.isEmpty() || isAutoComputed())
            description = commandInfo.getDescription();
        permission.merge(commandInfo.getPermission());
    }

}
