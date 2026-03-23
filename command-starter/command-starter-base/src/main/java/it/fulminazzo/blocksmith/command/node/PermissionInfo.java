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
    @NotNull Permission.Grant grant;

    @Getter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    boolean autoComputed;

    /**
     * Instantiates a new Permission info.
     *
     * @param permission the permission
     * @param grant      who the permission is granted to
     */
    public PermissionInfo(final @NotNull String permission,
                          final @NotNull Permission.Grant grant) {
        this(permission, grant, false);
    }

    /**
     * Instantiates a new Permission info.
     *
     * @param permission   the permission
     * @param grant        who the permission is granted to
     * @param autoComputed if <code>true</code>, will mark the permission as automatically computed,
     *                     meaning it was not provided by the user, but was instead retrieved from the command route
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    public PermissionInfo(final @NotNull String permission,
                          final @NotNull Permission.Grant grant,
                          final boolean autoComputed) {
        this.permission = permission;
        this.grant = grant;
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
        if (grant == Permission.Grant.OP)
            grant = permissionInfo.getGrant();
    }

}
