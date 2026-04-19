package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.message.Player;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

record PlayerReceiver(@NotNull Player internal) implements Receiver {

    @Override
    public @NotNull Audience toAudience() {
        return new Audience() {

            @Override
            public <T> void sendTitlePart(final @NotNull TitlePart<T> part,
                                          final @NotNull T value) {
                internal.getLastTitle().put(part, value);
            }

            @Override
            public void sendMessage(final @NotNull Identity source,
                                    final @NotNull Component message,
                                    final @NotNull MessageType type) {
                internal.setLastMessage(message);
            }

            @Override
            public void sendActionBar(final @NotNull Component message) {
                internal.setLastMessage(message);
            }

        };
    }

    @Override
    public @NotNull Locale getLocale() {
        return internal.getLocale();
    }

}
