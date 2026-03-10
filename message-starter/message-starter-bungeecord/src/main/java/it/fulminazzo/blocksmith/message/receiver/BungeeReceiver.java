package it.fulminazzo.blocksmith.message.receiver;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

@RequiredArgsConstructor
public final class BungeeReceiver implements Receiver {
    private static @Nullable BungeeAudiences adventure;

    private final @NotNull CommandSender receiver;

    @Override
    public @NotNull Audience toAudience() {
        return getAdventure().sender(receiver);
    }

    @Override
    public @NotNull Locale getLocale() {
        if (receiver instanceof ProxiedPlayer) return ((ProxiedPlayer) receiver).getLocale();
        else return Locale.getDefault();
    }

    /**
     * Initializes the internal serializer.
     *
     * @param plugin the plugin (using the receiver)
     */
    public static void setup(final @NotNull Plugin plugin) {
        adventure = BungeeAudiences.create(plugin);
    }

    static @NotNull BungeeAudiences getAdventure() {
        return Objects.requireNonNull(adventure, String.format("BungeeAudiences has not been initialized yet. Please use %s#startup(%s)",
                BungeeReceiver.class.getCanonicalName(), Plugin.class.getCanonicalName()
        ));
    }

}
