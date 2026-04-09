package it.fulminazzo.blocksmith.message.receiver;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ReceiverFactory} for Velocity platform.
 */
public final class VelocityReceiverFactory extends AbstractReceiverFactory {
    private static @Nullable ProxyServer server;

    @Override
    protected @NotNull Collection<Receiver> getAllReceiversImpl() {
        final ProxyServer server = getServer();
        return Stream.concat(
                server.getAllPlayers().stream(),
                Stream.of(server.getConsoleCommandSource())
        ).map(this::create).collect(Collectors.toList());
    }

    @Override
    protected @NotNull <R> Receiver createImpl(final @NotNull R receiver) {
        return new VelocityReceiver((CommandSource) receiver);
    }

    @Override
    protected boolean supportsImpl(final @NotNull Class<?> receiverType) {
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
