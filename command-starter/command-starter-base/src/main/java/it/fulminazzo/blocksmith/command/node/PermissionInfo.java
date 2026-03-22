package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
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
    final @NotNull Permission.Default permissionDefault;

    /**
     * Merges the given permission information into the current one
     * (only if the information are not given).
     *
     * @param permissionInfo the permission info to merge from
     */
    public void merge(final @NotNull PermissionInfo permissionInfo) {
        if (permission.isEmpty()) permission = permissionInfo.getPermission();

    }

}
