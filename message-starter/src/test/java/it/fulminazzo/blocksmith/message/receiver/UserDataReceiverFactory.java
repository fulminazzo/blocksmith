package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.message.UserData;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public final class UserDataReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull Collection<Receiver> getAllReceivers() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        return new Receiver() {

            @Override
            public @NotNull Audience toAudience() {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull Locale getLocale() {
                return ((UserData) receiver).getLocale();
            }

        };
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return UserData.class.isAssignableFrom(receiverType);
    }

}
