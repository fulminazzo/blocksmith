package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.message.argument.Time;
import lombok.RequiredArgsConstructor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public final class LastDeath implements TabExecutor {
    private final @NotNull Blocksmith plugin;

    @Override
    public boolean onCommand(final @NonNull CommandSender sender,
                             final @NonNull Command command,
                             final @NonNull String label,
                             final @NonNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int time = (int) (player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20.0 * 1000);
            plugin.getMessenger().sendMessage(player, "success.last-death", Time.of(time));
        } else plugin.getMessenger().sendMessage(sender, "error.console-cannot-execute");
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(final @NonNull CommandSender sender,
                                               final @NonNull Command command,
                                               final @NonNull String alias,
                                               final @NonNull String[] args) {
        return Collections.emptyList();
    }

}
