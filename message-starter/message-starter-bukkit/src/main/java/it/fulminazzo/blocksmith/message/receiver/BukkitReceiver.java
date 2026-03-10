package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@RequiredArgsConstructor
final class BukkitReceiver implements Receiver {
    private static @Nullable BukkitAudiences adventure;

    private final @NotNull CommandSender receiver;

    @Override
    public @NotNull Audience toAudience() {
        if (receiver instanceof Audience) return (Audience) receiver;
        else return getAdventure().sender(receiver);
    }

    @Override
    public @NotNull Locale getLocale() {
        if (receiver instanceof Player) return LocaleUtils.fromString(((Player) receiver).getLocale());
        else return Locale.getDefault();
    }

    static @NotNull BukkitAudiences getAdventure() {
        if (adventure == null)
            adventure = BukkitAudiences.create(JavaPlugin.getProvidingPlugin(BukkitReceiver.class));
        return adventure;
    }

}
