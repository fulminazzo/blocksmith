package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.command.CommandSender;
import it.fulminazzo.blocksmith.command.ConsoleCommandSender;
import it.fulminazzo.blocksmith.command.MockApplicationHandle;
import it.fulminazzo.blocksmith.command.Player;
import lombok.Value;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CommandSenderReceiverFactory extends AbstractReceiverFactory {
    private static final @NotNull List<CommandSender> senders = Arrays.asList(
            new ConsoleCommandSender(),
            new Player("Alex"),
            new Player("Camilla")
    );

    public CommandSenderReceiverFactory() {
        setup(new MockApplicationHandle());
    }

    @Override
    protected @NotNull Collection<Receiver> getAllReceiversImpl() {
        return senders.stream().map(this::createImpl).collect(Collectors.toSet());
    }

    @Override
    protected @NotNull <R> Receiver createImpl(final @NonNull R receiver) {
        return new CommandSenderReceiver((CommandSender) receiver);
    }

    @Override
    protected boolean supportsImpl(final @NotNull Class<?> receiverType) {
        return CommandSender.class.isAssignableFrom(receiverType);
    }

    @Value
    public static class CommandSenderReceiver implements Receiver {
        @NotNull CommandSender sender;

        @Override
        public @NotNull Audience toAudience() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NonNull <R> R getInternal() {
            return (R) sender;
        }

    }

}
