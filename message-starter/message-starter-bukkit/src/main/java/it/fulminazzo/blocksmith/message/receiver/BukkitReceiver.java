package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@RequiredArgsConstructor
final class BukkitReceiver implements Receiver {
    private final @NotNull BukkitAudiences adventure;
    private final @NotNull CommandSender internal;

    @Override
    public @NotNull Locale getLocale() {
        if (internal instanceof Player) return LocaleUtils.fromString(((Player) internal).getLocale());
        else return Locale.getDefault();
    }

    @Override
    public @NotNull Audience audience() {
        if (internal instanceof Audience) return (Audience) internal;
        else return adventure.sender(internal);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <R> R internal() {
        return (R) internal;
    }

}
