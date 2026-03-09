package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.message.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@RequiredArgsConstructor
final class PlayerReceiver implements Receiver {
    private final @NotNull Player player;

    @Override
    public @NotNull Audience toAudience() {
        return new Audience() {

            @Override
            public void sendMessage(final @NotNull Identity source,
                                    final @NotNull Component message,
                                    final @NotNull MessageType type) {
                player.setLastMessage(message);
            }

        };
    }

    @Override
    public @NotNull Locale getLocale() {
        return player.getLocale();
    }

}
