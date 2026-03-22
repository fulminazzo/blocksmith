package it.fulminazzo.blocksmith.command.node;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
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

    /**
     * Merges the given command information into the current one
     * (only if the information are not given).
     *
     * @param commandInfo the command info to merge from
     */
    public void merge(final @NotNull CommandInfo commandInfo) {
        if (description.isEmpty()) description = commandInfo.getDescription();
        permission.merge(commandInfo.getPermission());
    }

}
