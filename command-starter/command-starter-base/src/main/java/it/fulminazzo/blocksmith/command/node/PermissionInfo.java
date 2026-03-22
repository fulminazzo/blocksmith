package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the permission information.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter(AccessLevel.NONE)
public final class PermissionInfo {

    @NotNull String permission;
    @NotNull Permission.Default permissionDefault;

    @Getter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    boolean autoComputed;

    /**
     * Instantiates a new Permission info.
     *
     * @param permission        the permission
     * @param permissionDefault the permission default
     */
    public PermissionInfo(final @NotNull String permission,
                          final @NotNull Permission.Default permissionDefault) {
        this(permission, permissionDefault, false);
    }

    /**
     * Instantiates a new Permission info.
     *
     * @param permission        the permission
     * @param permissionDefault the permission default
     * @param autoComputed      if <code>true</code>, will mark the permission as automatically computed,
     *                          meaning it was not provided by the user, but was instead retrieved from the command route
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    public PermissionInfo(final @NotNull String permission,
                          final @NotNull Permission.Default permissionDefault,
                          final boolean autoComputed) {
        this.permission = permission;
        this.permissionDefault = permissionDefault;
        this.autoComputed = autoComputed;
    }

    /**
     * Merges the given permission information into the current one
     * (only if the information are not given).
     *
     * @param permissionInfo the permission info to merge from
     */
    public void merge(final @NotNull PermissionInfo permissionInfo) {
        if (permission.isEmpty() || isAutoComputed()) {
            permission = permissionInfo.getPermission();
            autoComputed = permissionInfo.isAutoComputed();
        }
        if (permissionDefault == Permission.Default.OP)
            permissionDefault = permissionInfo.getPermissionDefault();
    }

}
