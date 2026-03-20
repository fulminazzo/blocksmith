package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class PermissionInfo {
    @NotNull String permission;
    @NotNull Permission.Default permissionDefault;

}
