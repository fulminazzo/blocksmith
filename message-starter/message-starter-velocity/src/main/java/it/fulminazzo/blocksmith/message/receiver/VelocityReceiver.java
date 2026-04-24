package it.fulminazzo.blocksmith.message.receiver;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

@RequiredArgsConstructor
final class VelocityReceiver implements Receiver {
    @Getter
    private final @NotNull CommandSource internal;

    @Override
    public @NotNull Locale getLocale() {
        if (internal instanceof Player) return ((Player) internal).getPlayerSettings().getLocale();
        else return Locale.getDefault();
    }

    @Override
    public @NotNull Audience audience() {
        return internal;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <R> R handle() {
        return (R) internal;
    }

}
