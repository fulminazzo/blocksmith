package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.Blocksmith;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public final class HelloCommand implements TabExecutor {
    private final @NotNull Blocksmith plugin;

    @Override
    public boolean onCommand(final @NotNull CommandSender sender,
                             final @NotNull Command command,
                             final @NotNull String label,
                             final @NotNull String[] args) {
        plugin.getMessenger().sendMessage(sender, "greeting", Placeholder.of("name", sender.getName()));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(final @NotNull CommandSender sender,
                                               final @NotNull Command command,
                                               final @NotNull String alias,
                                               final @NotNull String[] args) {
        return Collections.emptyList();
    }

}
