package it.fulminazzo.blocksmith.message.receiver;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Implementation of {@link ReceiverFactory} for Velocity platform.
 */
public final class VelocityReceiverFactory implements ReceiverFactory {
    private static @Nullable ProxyServer server;

    @Override
    public @NotNull <R> Receiver create(final @NotNull R receiver) {
        return new VelocityReceiver((CommandSource) receiver);
    }

    @Override
    public boolean supports(final @NotNull Class<?> receiverType) {
        return CommandSource.class.isAssignableFrom(receiverType);
    }

    /**
     * Initializes the factory with the required fields.
     *
     * @param server the current server instance
     */
    public static void setup(final @NotNull ProxyServer server) {
        VelocityReceiverFactory.server = server;
    }

    /**
     * Gets the current server.
     *
     * @return the server
     */
    static @NotNull ProxyServer getServer() {
        return Objects.requireNonNull(server,
                "Message system has not been fully initialized yet. " +
                        String.format("Please use %s#startup(%s)",
                                VelocityReceiverFactory.class.getCanonicalName(), ProxyServer.class.getCanonicalName()
                        ));
    }

}
