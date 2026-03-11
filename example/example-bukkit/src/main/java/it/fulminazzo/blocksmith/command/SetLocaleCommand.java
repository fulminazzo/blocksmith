package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.Blocksmith;
import it.fulminazzo.blocksmith.BlocksmithUser;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import it.fulminazzo.blocksmith.scheduler.Scheduler;
import it.fulminazzo.blocksmith.structure.Pair;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@RequiredArgsConstructor
public final class SetLocaleCommand implements TabExecutor {
    private final @NotNull Blocksmith plugin;

    @Override
    public boolean onCommand(final @NonNull CommandSender sender,
                             final @NonNull Command command,
                             final @NonNull String label,
                             final @NonNull String[] args) {
        if (sender instanceof Player) {
            if (args.length < 1) {
                plugin.getMessenger().sendMessage(sender, "error.not-enough-arguments");
                plugin.getMessenger().sendMessage(sender, "usage", Placeholder.of("usage", command.getUsage()));
            } else {
                Player player = (Player) sender;
                Scheduler.runAsyncThen(
                        Pair.of(plugin, player),
                        plugin.getRepository().findById(player.getUniqueId()).thenCompose(u -> {
                            BlocksmithUser user = u.orElseGet(() -> new BlocksmithUser(player.getUniqueId(), player.getName(), null));
                            user.setLocale(LocaleUtils.fromString(args[0]));
                            return plugin.getRepository().save(user);
                        }),
                        u -> plugin.getMessenger().sendMessage(sender, "success.set-locale", Placeholder.of("locale", args[0]))
                ).exceptionally(t -> {
                    plugin.getLogger().log(Level.WARNING, "Error while executing task", t.getCause());
                    return null;
                });
            }
        } else plugin.getMessenger().sendMessage(sender, "error.console-cannot-execute");
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
