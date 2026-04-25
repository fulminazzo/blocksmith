package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.ServerApplication;
import it.fulminazzo.blocksmith.message.UserData;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public final class UserDataReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull ReceiverFactory setup(final @NotNull ServerApplication application) {
        return this;
    }

    @Override
    public @NotNull Collection<Receiver> getAllReceivers() {
        return new ArrayList<>();
    }

    @Override
    public <R> @NotNull Receiver create(final @NotNull R receiver) {
        return new Receiver() {

            @Override
            public @NotNull Locale getLocale() {
                return ((UserData) receiver).getLocale();
            }

            @Override
            public @NotNull Audience audience() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> @NotNull T handle() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return UserData.class.isAssignableFrom(receiverType);
    }

}
