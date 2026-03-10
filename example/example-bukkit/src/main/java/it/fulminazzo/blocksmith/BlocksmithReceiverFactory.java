package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.message.receiver.Receiver;
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactories;
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactory;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class BlocksmithReceiverFactory implements ReceiverFactory {

    @Override
    public @NotNull Collection<Receiver> getAllReceivers() {
        return Bukkit.getOnlinePlayers().stream().map(this::create).collect(Collectors.toList());
    }

    @Override
    public @NotNull <R> Receiver create(final @NonNull R receiver) {
        return new BlocksmithReceiver((Player) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return Player.class.isAssignableFrom(receiverType);
    }

    static final class BlocksmithReceiver implements Receiver {
        @Getter
        private final @NotNull Player internal;
        private final @NotNull Receiver defaultReceiver;

        public BlocksmithReceiver(final @NotNull Player player) {
            this.internal = player;
            this.defaultReceiver = ReceiverFactories.get(CommandSender.class).create(player);
        }

        @Override
        public @NotNull Audience toAudience() {
            return defaultReceiver.toAudience();
        }

        @Override
        public @NotNull Locale getLocale() {
            try {
                return Blocksmith.getInstance().getRepository().findById(internal.getUniqueId())
                        .thenApply(u -> u.map(BlocksmithUser::getLocale)
                                .orElseGet(defaultReceiver::getLocale)
                        )
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
