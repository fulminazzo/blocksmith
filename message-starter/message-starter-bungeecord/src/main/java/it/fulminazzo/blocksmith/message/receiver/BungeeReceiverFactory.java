package it.fulminazzo.blocksmith.message.receiver;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Implementation of {@link ReceiverFactory} for BungeeCord platform.
 */
public final class BungeeReceiverFactory implements ReceiverFactory {
    private static @Nullable BungeeAudiences adventure;

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        return new BungeeReceiver(getAdventure(), (CommandSender) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return CommandSender.class.isAssignableFrom(receiverType);
    }

    /**
     * Initializes the factory with the required fields.
     *
     * @param plugin the plugin using this factory
     */
    public static void setup(final @NotNull Plugin plugin) {
        adventure = BungeeAudiences.create(plugin);
    }

    /**
     * Gets the Text Adventure converter for command senders.
     *
     * @return the converter
     */
    static @NotNull BungeeAudiences getAdventure() {
        return Objects.requireNonNull(adventure,
                "Message system has not been fully initialized yet. " +
                        String.format("Please use %s#startup(%s)",
                                BungeeReceiverFactory.class.getCanonicalName(), Plugin.class.getCanonicalName()
                        ));
    }

}
