package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the permission information.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter(AccessLevel.NONE)
public final class PermissionInfo {

    @Nullable String prefix;
    @NotNull String actualPermission;
    @NotNull Permission.Grant grant;

    @Getter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    boolean autoComputed;

    /**
     * Instantiates a new Permission info.
     *
     * @param prefix     the prefix to prepend to the permission
     * @param permission the permission
     * @param grant      who the permission is granted to
     */
    public PermissionInfo(final @Nullable String prefix,
                          final @NotNull String permission,
                          final @NotNull Permission.Grant grant) {
        this(prefix, permission, grant, false);
    }

    /**
     * Instantiates a new Permission info.
     *
     * @param prefix       the prefix to prepend to the permission
     * @param permission   the permission
     * @param grant        who the permission is granted to
     * @param autoComputed if <code>true</code>, will mark the permission as automatically computed,
     *                     meaning it was not provided by the user, but was instead retrieved from the command route
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    public PermissionInfo(final @Nullable String prefix,
                          final @NotNull String permission,
                          final @NotNull Permission.Grant grant,
                          final boolean autoComputed) {
        this.prefix = prefix;
        this.actualPermission = permission;
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
        if (actualPermission.isEmpty() || isAutoComputed()) {
            prefix = permissionInfo.getPrefix();
            actualPermission = permissionInfo.getActualPermission();
            autoComputed = permissionInfo.isAutoComputed();
        }
        if (grant == Permission.Grant.OP)
            grant = permissionInfo.getGrant();
    }

    /**
     * Gets the permission (prefixed if {@link #prefix} has been provided).
     *
     * @return the permission
     */
    public @NotNull String getPermission() {
        return (prefix == null ? "" : prefix + ".") + actualPermission;
    }

}
