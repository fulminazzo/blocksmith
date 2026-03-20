package it.fulminazzo.blocksmith.command.node;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class CommandInfo {
    @NotNull String description;
    @NotNull PermissionInfo permission;

}
