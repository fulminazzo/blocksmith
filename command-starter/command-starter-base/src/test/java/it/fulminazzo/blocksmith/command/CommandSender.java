package it.fulminazzo.blocksmith.command;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@RequiredArgsConstructor
public class CommandSender {
    private final @NotNull String name;
    private final @NotNull List<String> permissions = new ArrayList<>();
    private boolean op;

    public CommandSender() {
        this("temp");
    }

    public @NotNull CommandSender addPermissions(final String @NotNull ... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
        return this;
    }

    public @NotNull CommandSender setOp(final boolean op) {
        this.op = op;
        return this;
    }

    public boolean hasPermission(final @NotNull String permission) {
        return permissions.stream().anyMatch(permission::equalsIgnoreCase);
    }

}
