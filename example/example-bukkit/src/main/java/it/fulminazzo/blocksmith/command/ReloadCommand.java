package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.Blocksmith;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public final class ReloadCommand implements TabExecutor {
    private final @NotNull Blocksmith plugin;

    @Override
    public boolean onCommand(final @NonNull CommandSender sender,
                             final @NonNull Command command,
                             final @NonNull String label,
                             final @NonNull String[] args) {
        plugin.disable();
        plugin.enable();
        return true;
    }

    @Override
    public @NonNull List<String> onTabComplete(final @NonNull CommandSender sender,
                                               final @NonNull Command command,
                                               final @NonNull String alias,
                                               final @NonNull String[] args) {
        return Collections.emptyList();
    }

}
