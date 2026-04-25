package it.fulminazzo.blocksmith.message.receiver;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@RequiredArgsConstructor
final class BungeeReceiver implements Receiver {
    private final @NotNull BungeeAudiences adventure;
    private final @NotNull CommandSender internal;

    @Override
    public @NotNull Locale getLocale() {
        if (internal instanceof ProxiedPlayer) return ((ProxiedPlayer) internal).getLocale();
        else return Locale.getDefault();
    }

    @Override
    public @NotNull Audience audience() {
        return adventure.sender(internal);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <R> R handle() {
        return (R) internal;
    }

}
